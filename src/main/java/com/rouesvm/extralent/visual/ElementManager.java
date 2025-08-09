package com.rouesvm.extralent.visual;

import com.rouesvm.extralent.visual.elements.InfoText;

import java.util.HashMap;
import java.util.UUID;

public class ElementManager {
    private final HashMap<UUID, InfoText> singularDisplay;

    public ElementManager() {
        this.singularDisplay = new HashMap<>();
    }

    public InfoText getElement(UUID uuid) {
        return singularDisplay.get(uuid);
    }

    public void createElement(UUID uuid, InfoText text) {
        singularDisplay.put(uuid, text);
    }

    public void removeElement(UUID uuid) {
        InfoText highlight = getElement(uuid);
        if (highlight != null) {
            highlight.setDestroy(true);
            singularDisplay.remove(uuid);
        }
    }
}
