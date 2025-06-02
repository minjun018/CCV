int GasPin = A0;
const int level_warning = 500;

void setup() {
  // put your setup code here, to run once:
//air
  pinMode(GasPin, INPUT);
  Serial.begin(9600); //시리얼 초기화
  int level_warning = 500;
}

void loop() {
  // put your main code here, to run repeatedly:
  float value = get_ppm();

  void led_blinking();
  if (value > level_warning)
  {
    Serial.println("ALERT");
  }
  Serial.println(value);
  delay(1000);
}

float get_ppm() {
  int raw = analogRead(GasPin);
   float voltage = raw * (5.0 / 1023.0);

  // 예제 공식: 측정 보정 필요
  float ppm = (voltage - 0.1) * 1000.0;
  return ppm;
}
