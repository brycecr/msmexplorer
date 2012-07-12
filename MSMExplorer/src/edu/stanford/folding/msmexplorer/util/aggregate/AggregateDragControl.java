/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.folding.msmexplorer.util.aggregate;

import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import prefuse.Display;
import prefuse.controls.DragControl;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualItem;

/**
 * Interactive drag control that is "aggregate-aware"
 * 
 * @auhtor Jeffrey Heer
 */
public class AggregateDragControl extends DragControl {

        private VisualItem activeItem;

        /**
         * Creates a new drag control that issues repaint requests as an item
         * is dragged.
         */
        public AggregateDragControl() {
        }

        /**
         * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
         */
        @Override
        public void itemDragged(VisualItem item, MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                        return;
                }
                dragged = true;
                Display d = (Display) e.getComponent();
                d.getAbsoluteCoordinate(e.getPoint(), temp);
                double dx = temp.getX() - down.getX();
                double dy = temp.getY() - down.getY();

                move(item, dx, dy);

                down.setLocation(temp);
        }

        protected static void setFixed(VisualItem item, boolean fixed) {
                if (item instanceof AggregateItem) {
                        Iterator items = ((AggregateItem) item).items();
                        while (items.hasNext()) {
                                setFixed((VisualItem) items.next(), fixed);
                        }
                } else {
                        item.setFixed(fixed);
                }
        }

        public void itemPressed(VisualItem item, MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                        return;
                }
                dragged = false;
                Display d = (Display) e.getComponent();
                d.getAbsoluteCoordinate(e.getPoint(), down);
                setFixed(item, true);
        }

        protected static void move(VisualItem item, double dx, double dy) {
                if (item instanceof AggregateItem) {
                        Iterator<VisualItem> items = ((AggregateItem) item).items();
                        while (items.hasNext()) {
                                move(items.next(), dx, dy);
                        }
                } else {
                        double x = item.getX();
                        double y = item.getY();
                        item.setStartX(x);
                        item.setStartY(y);
                        item.setX(x + dx);
                        item.setY(y + dy);
                        item.setEndX(x + dx);
                        item.setEndY(y + dy);
                }
        }
} // end of class AggregateDragControl