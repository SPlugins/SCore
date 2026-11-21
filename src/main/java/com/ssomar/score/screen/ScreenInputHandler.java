package com.ssomar.score.screen;

/** Receives the inputs of a screen, on the player's thread (main thread, or its region on Folia). */
public interface ScreenInputHandler {
    void onInput(ScreenSession session, ScreenInput input);
}
