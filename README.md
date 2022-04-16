# SnakeGame_UsingGuesture
## 動機
#### 貪食蛇遊戲想必是大家的童年，最近在回憶時想到小時候玩貪食蛇是在電腦上玩的，以方向鍵操作貪食蛇吃食物。網路上很多貪食蛇的遊戲也是以方向鍵操作，最大的差別在於遊戲畫面的精美度，玩法幾乎是一成不變，這時我想到如果只要動動手勢去命令貪食蛇要往哪個方向行走是一個不錯的主意，也是一種新玩法，加上最近在學以Kotlin開發Android App，因此我投入"以手勢控制貪食蛇"的手遊app開發。
## 玩法
#### 操作手勢以豎起大拇指為主要手勢，大拇指往上即操作蛇往上行走，往右即操作蛇往右行走，以此類推。其餘玩法跟其他貪食蛇遊戲相同，蛇吃到食物身體會變長並且移動速度加快，碰到牆壁或是自身就死亡。
## 實作流程
#### 分為兩部分，第一部分為貪食蛇遊戲之邏輯，第二部分為辨識手勢之功能
###### 1. 遊戲中所有物件皆有獨立一個類別，比如食物有專屬一個類別，所有類別皆繼承GameObject類別，因為此類別定義所有物件皆會有的父方法、屬性。遊戲背景的繪製勢使用Canvas作為畫布，再以各物件各自定義的Paint物件繪製出各物件的樣貌。遊戲中的物件的繪製是以一格格方塊為基礎繪製的，因為此專案的遊戲風格為類似像素風格的遊戲，將在layout中元件給定的長寬除以設定的長寬之格數來定義每一格的座標。蛇的移動採用執行緒休眠的方式繪製蛇在下一個格子中，執行緒休眠的時間是根據蛇吃到食物的次數來減少。
###### 2. 透過實作ImageAnalysis.Analyzer介面即時獲取相機鏡頭的輸入流，對每一幀影像應用圖像辨識tensorflow lite的推理，計算該幀影像在每一類別中的confidence，獲取其中最大的confidence對應之label作為該幀的類別，並以類別作為function parameter呼叫貪食蛇移動的函式，如果傳入的類別為right，則在函式內部控制蛇往右行走。
## Demo
![3b71a18f-2545-4e47-a2b9-fcc246ca5ae8_AdobeCreativeCloudExpress](https://user-images.githubusercontent.com/68068287/163668599-74eeda62-b278-485d-9031-f4c97c41d189.gif)


