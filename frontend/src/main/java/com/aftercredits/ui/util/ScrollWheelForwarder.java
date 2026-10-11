package com.aftercredits.ui.util;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Utility that fixes mouse-wheel scrolling in Swing scroll panes whose child
 * components (panels, labels, cards, buttons) would otherwise silently consume
 * wheel events without forwarding them to the enclosing JScrollPane.
 *
 * <p>Usage — call once on the JScrollPane you want scrollable:
 * <pre>
 *     ScrollWheelForwarder.install(myScrollPane);
 * </pre>
 * This recursively attaches a forwarding listener to every current and future
 * child component inside the scroll pane's viewport.
 */
public final class ScrollWheelForwarder {

    private ScrollWheelForwarder() {}

    /**
     * Installs mouse-wheel forwarding on {@code scrollPane} and all of its
     * current descendants. Any new components added later should also call
     * {@link #installOn(Component, JScrollPane)} individually, or the parent
     * scroll pane will already intercept the event if no child has a custom
     * wheel listener.
     */
    public static void install(JScrollPane scrollPane) {
        // Forward wheel events that land directly on the scroll pane itself
        attachForwarder(scrollPane, scrollPane);
        // Recursively cover every component currently in the viewport
        if (scrollPane.getViewport() != null && scrollPane.getViewport().getView() != null) {
            installOnTree(scrollPane.getViewport().getView(), scrollPane);
        }
    }

    /**
     * Installs mouse-wheel forwarding on a single component and all of its
     * descendants so that wheel events bubble up to the given target scroll pane.
     */
    public static void installOn(Component root, JScrollPane targetScrollPane) {
        installOnTree(root, targetScrollPane);
    }

    // ── internal ────────────────────────────────────────────────────────────

    private static void installOnTree(Component component, JScrollPane targetScrollPane) {
        attachForwarder(component, targetScrollPane);
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                installOnTree(child, targetScrollPane);
            }
        }
    }

    private static void attachForwarder(Component component, JScrollPane targetScrollPane) {
        // Avoid duplicate listeners
        for (MouseWheelListener existing : component.getMouseWheelListeners()) {
            if (existing instanceof ForwardingWheelListener) return;
        }
        component.addMouseWheelListener(new ForwardingWheelListener(targetScrollPane));
    }

    // ── listener ────────────────────────────────────────────────────────────

    private static final class ForwardingWheelListener implements MouseWheelListener {
        private final JScrollPane target;

        ForwardingWheelListener(JScrollPane target) {
            this.target = target;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            // Re-dispatch the event to the target scroll pane so its vertical
            // scrollbar responds correctly regardless of which child had focus.
            MouseWheelEvent forwarded = new MouseWheelEvent(
        target,
        e.getID(),
        e.getWhen(),
        e.getModifiersEx(),
        e.getX(), e.getY(),
        e.getXOnScreen(), e.getYOnScreen(),
        e.getClickCount(),
        e.isPopupTrigger(),
        e.getScrollType(),
        e.getScrollAmount(),
        e.getWheelRotation(),
        e.getPreciseWheelRotation()
);
            SwingUtilities.invokeLater(() -> target.dispatchEvent(forwarded));
            e.consume(); // prevent double-scrolling if scroll pane also receives original
        }
    }
}
