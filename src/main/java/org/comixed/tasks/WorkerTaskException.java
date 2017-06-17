/*
 * ComixEd - A digital comic book library management application.
 * Copyright (C) 2017, Darryl L. Pierce
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.package
 * org.comixed;
 */

package org.comixed.tasks;

/**
 * <code>WorkerTaskException</code> is raised when an error occurs while
 * performing a {@link WorkerTask}.
 *
 * @author Darryl L. Pierce
 *
 */
public class WorkerTaskException extends Exception
{
    private static final long serialVersionUID = 7919030698664175776L;

    public WorkerTaskException(String message, Exception cause)
    {
        super(message, cause);
    }
}
