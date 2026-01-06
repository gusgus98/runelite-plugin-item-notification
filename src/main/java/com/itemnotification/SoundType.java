package com.itemnotification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.SoundEffectID;

@Getter
@RequiredArgsConstructor
public enum SoundType {
    BOOP("Boop", SoundEffectID.UI_BOOP, null),
    ELITE_LEVEL_UP("Level Up", 98, null),
    GE_COIN_DING("Coin Ding", 3925, null),
    TREE_FALLING("Tree Falling", 2734, null),
    UNIQUE_JINGLE("Unique Jingle (Custom)", -1, "/Dt2Jingle.wav");

    private final String name;
    private final int validSoundId;
    private final String resourcePath;

    @Override
    public String toString() {
        return name;
    }
}
