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

package org.comixed.ui.menus;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.comixed.ui.menus.MenuHelper.Menu;
import org.comixed.ui.menus.MenuHelper.MenuType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * <code>MainMenuBar</code> manages the menu bar for the main application
 * window.
 *
 * @author Darryl L. Pierce
 *
 */
@Component
@PropertySource("classpath:menus.properties")
@ConfigurationProperties("app.menus")
public class MainMenuBar extends JMenuBar implements
                         InitializingBean
{
    private static final long serialVersionUID = -3549352202994937250L;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<Menu> mainMenu = new ArrayList<>();

    @Autowired
    private MenuHelper menuHelper;

    @Autowired
    private ApplicationContext context;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        // build the menu
        JMenu menu = null;
        Menu lastItem = null;
        for (Menu item : this.mainMenu)
        {
            // skip any undefined item due to the number in
            // application.properties
            if (item.menu == null)
            {
                continue;
            }

            // create the menu if necessary
            if ((lastItem == null) || !lastItem.menu.equals(item.menu))
            {
                this.logger.debug("Creating menu: " + item.menu);
                menu = new JMenu();
                this.menuHelper.configureMenuItem(menu, item.menu);
                this.add(menu);
            }

            if (item.type == MenuType.SEPARATOR)
            {
                this.logger.debug("Creating menu item: " + item.menu + "->" + item.label);
                menu.addSeparator();
            }
            else if (item.type == MenuType.SUBMENU)
            {
                this.logger.debug("Creating submenu item: " + item.menu + "->" + item.label);
                if (this.context.containsBean(item.bean))
                {
                    menu.add((JMenu )this.context.getBean(item.bean));
                }
                else
                {
                    this.logger.warn("No such bean: " + item.bean);
                }
            }
            else if (item.type == MenuType.ITEM)
            {
                JMenuItem menuItem = this.menuHelper.createMenuItem(item.menu + "." + item.label, item.bean);
                if (menuItem != null)
                {
                    menu.add(menuItem);
                }
            }

            lastItem = item;
        }
    }

    public List<Menu> getMainMenu()
    {
        return this.mainMenu;
    }
}
