/*
 * Copyright 2015-2020 Ray Fowler
 * Modifications Copyright 2024 Ilya Zushinskiy
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
package rotp.model.tech;

import rotp.model.empires.Empire;

public final class TechSubspaceInterdictor extends Tech {
    public TechSubspaceInterdictor(String typeId, int lv, int seq, boolean b, TechCategory c) {
        super(c, Tech.SUBSPACE_INTERDICTOR, typeId, seq, lv);
        free = b;
    }
    @Override
    public float warModeFactor()        { return 1.5f; }
    @Override
    public boolean isObsolete(Empire c) {
        return c.tech().subspaceInterdiction();
    }
    @Override
    public float baseValue(Empire c) { return c.ai().scientist().baseValue(this); }
    @Override
    public void provideBenefits(Empire c) {
        super.provideBenefits(c);
        if (!isObsolete(c))
            c.tech().topSubspaceInterdictorTech(this);
    }
}
