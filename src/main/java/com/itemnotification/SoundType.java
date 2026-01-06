package com.itemnotification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SoundType {
    UNIQUE_JINGLE("Unique Jingle", "/Dt2Jingle.wav");

    private final String name;
    private final String resourcePath;

    @Override
    public String toString() {
        return name;
    }
}
