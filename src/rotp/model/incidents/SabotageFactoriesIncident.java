/*
 * Copyright 2015-2020 Ray Fowler
 * Modifications Copyright 2023-2024 Ilya Zushinskiy
 * 
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.gnu.org/licenses/gpl-3.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.model.incidents;

import rotp.model.empires.DiplomaticEmbassy;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.empires.SabotageMission;
import rotp.model.galaxy.StarSystem;
import rotp.ui.diplomacy.DialogueManager;
import rotp.ui.notifications.FactoriesDestroyedAlert;

public class SabotageFactoriesIncident extends DiplomaticIncident {
    private static final long serialVersionUID = 1L;
    private final int empVictim;
    private final int empSpy;
    private final int sysId;
    private final int destroyed;

    public static void addIncident(SabotageMission m) {
        EmpireView otherView = m.spies().view().otherView();
        otherView.embassy().resetAllianceTimer();
        otherView.embassy().resetPactTimer();
        // no incident if spy not caught
        if (!m.spy().caught()) {
            Empire victim = otherView.owner();
            if (victim.isPlayerControlled()
            && (m.factoriesDestroyed() > 0)) {
                StarSystem sys = m.starSystem();
                FactoriesDestroyedAlert.create(null, m.factoriesDestroyed(), sys);
            }
            return;
        }

        // create incident and add to victim's empireView
        otherView.embassy().addIncident(new SabotageFactoriesIncident(otherView, m));
    }
    private SabotageFactoriesIncident(EmpireView ev, SabotageMission m) {
        super(calculateSeverity(ev, m));
        empVictim = ev.owner().id;
        empSpy = ev.empire().id;
        sysId = m.starSystem().id;
        destroyed = m.factoriesDestroyed();

        if (ev.owner().isPlayerControlled() && (destroyed > 0)) {
            StarSystem sys = m.starSystem();
            FactoriesDestroyedAlert.create(ev.empire(), destroyed, sys);
            if (sys.isColonized() && sys.colony().defense().allocation() == 0) {
                String str1 = text("MAIN_ALLOCATE_SABOTAGE_FACTORIES", systemName(), str(destroyed), ev.empire().raceName());
                str1 = ev.empire().replaceTokens(str1, "spy");
                session().addSystemToAllocate(sys, str1);
            }
        }
    }
    private static float calculateSeverity(EmpireView view, SabotageMission m) {
        float multiplier = view.empire().leader().isIndustrialist() ? 2 : 1;
        return Math.max(-20,(-1*m.factoriesDestroyed())+view.embassy().currentSpyIncidentSeverity()) * multiplier;
    }
    private String systemName()      { return player().sv.name(sysId); }
    @Override
    public boolean isSpying()        { return true; }
    @Override
    public int timerKey()          { return DiplomaticEmbassy.TIMER_SPY_WARNING; }
    @Override
    public String title()            { return text("INC_DESTROYED_FACTORIES_TITLE"); }
    @Override
    public String description()      { return decode(text("INC_DESTROYED_FACTORIES_DESC")); }
    @Override
    public String warningMessageId() { return galaxy().empire(empVictim).isPlayerControlled() ? "" : DialogueManager.WARNING_SABOTAGE; }
    @Override
    public String declareWarId()     { return DialogueManager.DECLARE_SPYING_WAR; }
    @Override
    public String decode(String s) {
        String s1 = super.decode(s);
        s1 = galaxy().empire(empSpy).replaceTokens(s1, "spy");
        s1 = galaxy().empire(empVictim).replaceTokens(s1, "victim");
        s1 = s1.replace("[system]", systemName());
        s1 = s1.replace("[amt]", str(destroyed));
        s1 = s1.replace("[target]", text("SABOTAGE_FACTORIES"));
        return s1;
    }
}
