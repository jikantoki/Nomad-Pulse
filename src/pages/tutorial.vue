<template lang="pug">
div.tutorial-root
  //- ページインジケーター（上部）
  .tutorial-header
    .page-dots
      span.dot(
        v-for="i in totalPages"
        :key="i"
        :class="{ active: currentPage === i - 1 }"
        @click="currentPage = i - 1"
      )
    v-btn(
      v-if="currentPage < totalPages - 1"
      variant="text"
      size="small"
      @click="$router.push('/')"
    ) スキップ

  //- メインコンテンツ（スライド）
  v-window(
    v-model="currentPage"
    direction="horizontal"
    style="flex: 1; overflow: hidden;"
  )
    //- ページ1: ようこそ
    v-window-item(:value="0")
      .slide
        .slide-icon-large
          img(src="/icon.png" style="width: 100px; height: 100px; border-radius: 20px;")
        h1.slide-title Nomad Pulseへ
        h1.slide-title ようこそ！
        p.slide-description
          | 友達とリアルタイムで位置情報を共有できる
          br
          | 新感覚コミュニケーションアプリです。
        .feature-chips
          v-chip.ma-1(prepend-icon="mdi-map-marker" color="primary" variant="tonal") リアルタイム共有
          v-chip.ma-1(prepend-icon="mdi-account-multiple" color="primary" variant="tonal") 友達とつながる
          v-chip.ma-1(prepend-icon="mdi-shield-lock" color="primary" variant="tonal") プライバシー設定

    //- ページ2: 基本操作
    v-window-item(:value="1")
      .slide
        .slide-icon
          v-icon(size="64" color="white") mdi-map-marker
        h2.slide-title 地図で今どこにいるか確認！
        .instruction-list
          .instruction-item
            v-icon.instruction-icon(color="primary") mdi-crosshairs-gps
            div
              p.instruction-label 現在地を表示
              p.instruction-text 地図上にあなたのアイコンが表示されます
          .instruction-item
            v-icon.instruction-icon(color="primary") mdi-gesture-pinch
            div
              p.instruction-label ズームで詳細表示
              p.instruction-text ズームレベル15以上で名前が表示されます
          .instruction-item
            v-icon.instruction-icon(color="primary") mdi-map-marker-radius
            div
              p.instruction-label 現在地ボタン
              p.instruction-text 右下のボタンをタップすると現在地に戻ります

    //- ページ3: 友達の追加
    v-window-item(:value="2")
      .slide
        .slide-icon.green-bg
          v-icon(size="64" color="white") mdi-account-plus
        h2.slide-title 友達を追加しよう！
        .instruction-list
          .instruction-item
            .step-number 1
            div
              p.instruction-label メニューを開く
              p.instruction-text 右上のアイコンをタップしてメニューを開きます
          .instruction-item
            .step-number 2
            div
              p.instruction-label 友達を探す
              p.instruction-text 「友達を探す」または「QRコードで友達を探す」を選択
          .instruction-item
            .step-number 3
            div
              p.instruction-label IDで検索
              p.instruction-text 友達のIDを入力して検索、またはQRコードをスキャン
          .instruction-item
            .step-number 4
            div
              p.instruction-label 承認を待つ
              p.instruction-text 友達がリクエストを承認したら位置情報の共有が始まります

    //- ページ4: 友達を地図で見つける
    v-window-item(:value="3")
      .slide
        .slide-icon.orange-bg
          v-icon(size="64" color="white") mdi-map-search
        h2.slide-title 友達の場所をチェック！
        .instruction-list
          .instruction-item
            v-icon.instruction-icon(color="primary") mdi-account-circle
            div
              p.instruction-label 地図上のアイコン
              p.instruction-text 友達のプロフィール写真が地図上に表示されます
          .instruction-item
            v-icon.instruction-icon(color="primary") mdi-gesture-tap
            div
              p.instruction-label タップして詳細確認
              p.instruction-text アイコンをタップするとバッテリー残量・最終更新時間などの詳細情報が表示されます
          .instruction-item
            v-icon.instruction-icon(color="primary") mdi-google-maps
            div
              p.instruction-label Google Mapsで開く
              p.instruction-text 詳細カードの「Google Mapsで開く」ボタンでナビゲーションができます

    //- ページ5: 便利な機能 & 使ってみる
    v-window-item(:value="4")
      .slide
        .slide-icon.purple-bg
          v-icon(size="64" color="white") mdi-star
        h2.slide-title 他にもこんな機能が！
        .instruction-list
          .instruction-item
            v-icon.instruction-icon(color="primary") mdi-chart-timeline-variant
            div
              p.instruction-label タイムライン
              p.instruction-text 自分の移動履歴を時系列で確認できます
          .instruction-item
            v-icon.instruction-icon(color="primary") mdi-clock-outline
            div
              p.instruction-label 時間・場所で共有を制限
              p.instruction-text 指定した時間帯や場所でのみ位置情報を共有できます
          .instruction-item
            v-icon.instruction-icon(color="primary") mdi-battery-charging
            div
              p.instruction-label バッテリー情報を共有
              p.instruction-text 友達のバッテリー残量・充電状況も確認できます
        v-btn.start-btn(
          size="x-large"
          color="primary"
          rounded="pill"
          prepend-icon="mdi-rocket-launch"
          @click="$router.push('/')"
        ) 使ってみる

  //- ナビゲーションボタン（下部）
  .tutorial-footer
    v-btn(
      v-if="currentPage > 0"
      variant="outlined"
      rounded="pill"
      prepend-icon="mdi-chevron-left"
      @click="currentPage--"
    ) 戻る
    v-spacer
    v-btn(
      v-if="currentPage < totalPages - 1"
      color="primary"
      rounded="pill"
      append-icon="mdi-chevron-right"
      @click="currentPage++"
    ) 次へ
</template>

<script lang="ts">
  export default {
    data () {
      return {
        currentPage: 0,
        totalPages: 5,
      }
    },
  }
</script>

<style lang="scss" scoped>
.tutorial-root {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 100vh;
  background: rgb(var(--v-theme-background));
}

.tutorial-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 8px;

  .page-dots {
    display: flex;
    gap: 8px;

    .dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      background-color: rgba(var(--v-theme-on-surface), 0.2);
      cursor: pointer;
      transition: background-color 0.3s, transform 0.3s;

      &.active {
        background-color: rgb(var(--v-theme-primary));
        transform: scale(1.3);
      }
    }
  }
}

.tutorial-footer {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  gap: 8px;
}

.slide {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 24px 8px;
  min-height: calc(100vh - 120px);
  overflow-y: auto;
}

.slide-icon-large {
  margin-bottom: 24px;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.15));
}

.slide-icon {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: rgb(var(--v-theme-primary));
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(var(--v-theme-primary), 0.4);

  &.green-bg {
    background: #4caf50;
    box-shadow: 0 4px 20px rgba(76, 175, 80, 0.4);
  }

  &.orange-bg {
    background: #ff9800;
    box-shadow: 0 4px 20px rgba(255, 152, 0, 0.4);
  }

  &.purple-bg {
    background: #9c27b0;
    box-shadow: 0 4px 20px rgba(156, 39, 176, 0.4);
  }
}

.slide-title {
  font-size: 1.6em;
  font-weight: bold;
  text-align: center;
  margin: 0 0 8px;
  line-height: 1.3;
}

.slide-description {
  text-align: center;
  font-size: 1em;
  line-height: 1.7;
  color: rgba(var(--v-theme-on-surface), 0.7);
  margin: 16px 0 24px;
}

.feature-chips {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 8px;
}

.instruction-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 24px;
}

.instruction-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  background: rgba(var(--v-theme-surface-variant), 0.5);
  border-radius: 12px;
  padding: 14px 16px;

  .instruction-icon {
    margin-top: 2px;
    flex-shrink: 0;
  }

  .instruction-label {
    font-weight: 600;
    font-size: 0.95em;
    margin: 0 0 4px;
  }

  .instruction-text {
    font-size: 0.85em;
    color: rgba(var(--v-theme-on-surface), 0.7);
    margin: 0;
    line-height: 1.5;
  }
}

.step-number {
  width: 32px;
  height: 32px;
  min-width: 32px;
  border-radius: 50%;
  background: rgb(var(--v-theme-primary));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.95em;
  margin-top: 2px;
}

.start-btn {
  width: 100%;
  margin-top: 32px;
  font-size: 1.1em;
  font-weight: bold;
}
</style>
