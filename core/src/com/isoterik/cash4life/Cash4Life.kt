package com.isoterik.cash4life

import com.badlogic.gdx.graphics.Texture
import io.github.isoteriktech.xgdx.Scene
import io.github.isoteriktech.xgdx.XGdxGame
import io.github.isoteriktech.xgdx.x2d.scenes.transition.SceneTransitions


class Cash4Life : XGdxGame() {
    override fun initGame(): Scene {
        xGdx.defaultSettings.VIEWPORT_WIDTH = GlobalConstants.GUI_WIDTH.toFloat()
        xGdx.defaultSettings.VIEWPORT_HEIGHT = GlobalConstants.GUI_HEIGHT.toFloat()

        loadDoubleCashAssets()
        xGdx.assets.loadAssetsNow()

        splashTransition = SceneTransitions.fade(1f)
        return MainScene()
    }

    private fun loadDoubleCashAssets() {
        xGdx.assets.enqueueFolderContents("${GlobalConstants.DOUBLE_CASH_ASSETS_HOME}/images", Texture::class.java)
        xGdx.assets.enqueueAtlas("${GlobalConstants.DOUBLE_CASH_ASSETS_HOME}/spritesheets/cards.atlas")
        xGdx.assets.enqueueSkin(GlobalConstants.DOUBLE_CASH_SKIN)
        xGdx.assets.enqueueSfxFolder("${GlobalConstants.DOUBLE_CASH_ASSETS_HOME}/sfx")
    }
}