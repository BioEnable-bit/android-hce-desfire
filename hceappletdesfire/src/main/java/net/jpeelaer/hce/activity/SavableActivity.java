/*
 * Copyright 2014 Gerhard Klostermeier
 * Copyright 2015 Jeroen Peelaerts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package net.jpeelaer.hce.activity;

/**
 * Interface with callback functions for objects (most likely Activities) that
 * want to use the {@link ActivityUtil#checkFileExistenceAndSave(java.io.File,
 * String[],boolean, android.content.Context, SavableActivity)}.
 * @author Gerhard Klostermeier
 */
public interface SavableActivity {

    /**
     * This method will be called after a successful save process.
     */
    public abstract void onSaveSuccessful();

    /**
     * This method will be called, if there was an error during the
     * save process or it the user hits "cancel" in the "file already exists"
     * dialog.
     */
    public abstract void onSaveFailure();
}
