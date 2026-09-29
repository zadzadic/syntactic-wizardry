package com.arcane.magic.client;

/**
 * The legacy Builder render hook also invoked Arcane's spell Field renderer.
 * Syntactic Wizardry has its own spell renderer, so the Builder compatibility
 * bridge deliberately leaves that unrelated call empty.
 */
public final class FieldClientRenderRuntime {
    private FieldClientRenderRuntime() {}
    public static void render(Object event) {}
}
