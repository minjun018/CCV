int pulsePin = A0;       // Pulse Sensor가 연결된 핀
int signal;              // 측정된 신호 값
int threshold = 550;     // 박동 감지 임계값 (상황에 따라 조정 필요)

unsigned long lastBeatTime = 0;
int beatCount = 0;
int bpm = 0;

void setup() {
  Serial.begin(9600);
  pinMode(pulsePin, INPUT);
}

void loop() {
  signal = analogRead(pulsePin);

  // 박동 감지: threshold 초과 + 최소 간격(300ms) 유지
  if (signal > threshold && millis() - lastBeatTime > 300) {
    lastBeatTime = millis();
    beatCount++;
    Serial.println("♥ Beat detected");
  }

  // 10초간 측정 후 BPM 계산
  static unsigned long startTime = millis();
  if (millis() - startTime >= 10000) {
    bpm = beatCount * 6;  // 10초간 측정한 박동 수 * 6 = 60초 기준
    Serial.print("BPM: ");
    Serial.println(bpm);

    // 경고 조건
    if (bpm < 55 || bpm > 100) {
      Serial.println("ALERT");
    }

    // 초기화
    beatCount = 0;
    startTime = millis();
  }

  delay(20);  // 너무 빠르게 반복하지 않도록 잠시 지연
}
