package io.github.keufcp;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServerUtils MODのクライアント側メインクラス．
 *
 * <p>クライアント側では最小限の初期化のみを行います． 主要な機能はServerUtils（サーバー側）が担当します．
 */
public class ServerUtilsClient implements ClientModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger(ServerUtils.MOD_ID + "-client");

  @Override
  public void onInitializeClient() {
    LOGGER.info("{}-client is initialized.", ServerUtils.MOD_ID);
    // クライアント側では最小限の初期化のみ行い，主要な処理はサーバー側に任せる
  }
}
