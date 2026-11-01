package com.aftercredits.ui.util;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Safe mouse-wheel forwarding utility for Swing scroll panes.
 *
 * Instead of redispatching MouseWheelEvents back into a JScrollPane,
 * this utility directly updates the appropriate scrollbar. This avoids
 * recursive wheel-event dispatch and the jitter it can cause.
 */
public final class ScrollWheelForwarder {

    private ScrollWheelForwarder() {}

    /**
     * Installs vertical scrolling on a scroll pane and its descendants.
     */
    public static void install(JScrollPane scrollPane) {
        if (scrollPane == null) return;

        installOnTree(scrollPane.getViewport().getView(), scrollPane);
    }

    /**
     * Installs scrolling on a component tree for the supplied target pane.
     */
    public static void installOn(Component root, JScrollPane targetScrollPane) {
        if (root == null || targetScrollPane == null) return;

        installOnTree(root, targetScrollPane);
    }

    private static void installOnTree(Component component, JScrollPane targetScrollPane) {
        if (component == null) return;

        attachForwarder(component, targetScrollPane);

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                installOnTree(child, targetScrollPane);
            }
        }
    }

    private static void attachForwarder(
            Component component,
            JScrollPane targetScrollPane) {

        for (MouseWheelListener existing : component.getMouseWheelListeners()) {
            if (existing instanceof ForwardingWheelListener) {
                return;
            }
        }

        component.addMouseWheelListener(
                new ForwardingWheelListener(targetScrollPane)
        );
    }

    private static final class ForwardingWheelListener
            implements MouseWheelListener {

        private final JScrollPane target;

        ForwardingWheelListener(JScrollPane target) {
            this.target = target;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int rotation = e.getWheelRotation();

            // Shift + wheel is treated as horizontal scrolling when
            // the target has a usable horizontal scrollbar.
            boolean horizontal =
                    e.isShiftDown()
                    && target.getHorizontalScrollBar().getMaximum()
                    > target.getHorizontalScrollBar().getVisibleAmount();

            JScrollBar bar = horizontal
                    ? target.getHorizontalScrollBar()
                    : target.getVerticalScrollBar();

            if (bar.getMaximum() <= bar.getVisibleAmount()) {
                return;
            }

            int amount = Math.max(
                    bar.getUnitIncrement(),
                    bar.getVisibleAmount() / 8
            );

            int newValue = bar.getValue() + rotation * amount;

            newValue = Math.max(
                    bar.getMinimum(),
                    Math.min(
                            newValue,
                            bar.getMaximum() - bar.getVisibleAmount()
                    )
            );

            bar.setValue(newValue);
            e.consume();
        }
    }
}
