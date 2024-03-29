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

package org.comixed.ui.components;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.comixed.library.model.ComicSelectionModel;
import org.comixed.library.model.ComicTableModel;
import org.comixed.library.model.Page;
import org.comixed.ui.menus.MenuHelper;
import org.comixed.ui.menus.MenuHelper.Menu;
import org.comixed.ui.menus.MenuHelper.MenuType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * <code>ComicDetailsTable</code> shows the details for a selection of comics.
 *
 * @author Darryl L. Pierce
 *
 */
@Component
@PropertySource("classpath:menus.properties")
@ConfigurationProperties("app.comic-details-view.popup")
public class ComicDetailsTable extends JTable implements
                               InitializingBean
{
    private static final long serialVersionUID = -4512908003749212065L;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ComicTableModel comicTableModel;
    @Autowired
    private ComicSelectionModel comicSelectionModel;
    @Autowired
    private MenuHelper menuHelper;
    @Autowired
    private TableCellPageRenderer pageRenderer;
    private List<Menu> menu = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception
    {
        this.logger.debug("Connecting comic table to underlying model");
        this.setModel(this.comicTableModel);
        this.logger.debug("Subscribing selection model to table view updates");
        this.getSelectionModel().addListSelectionListener(this.comicSelectionModel);
        this.logger.debug("Building comic table view popup menu");
        JPopupMenu popup = new JPopupMenu();

        for (Menu item : this.menu)
        {
            this.logger.debug("Menu item type: " + item.type);
            if (item.label == null)
            {
                continue;
            }
            if (item.type == MenuType.SEPARATOR)
            {
                this.logger.debug("Creating menu separator");
                popup.addSeparator();
            }
            else if (item.type == MenuType.ITEM)
            {
                this.logger.debug("Menu item label: " + item.label);
                this.logger.debug("Menu item bean: " + item.bean);
                JMenuItem menuItem = this.menuHelper.createMenuItem(item.label, item.bean);
                if (menuItem != null)
                {
                    popup.add(menuItem);
                }
            }
        }

        this.setComponentPopupMenu(popup);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column)
    {
        Object obj = this.getValueAt(row, column);
        Class<?> clazz = obj != null ? obj.getClass() : null;

        if (clazz == Page.class) return this.pageRenderer;
        else return super.getCellRenderer(row, column);
    }

    public List<Menu> getMenu()
    {
        return this.menu;
    }
}