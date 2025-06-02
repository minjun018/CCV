#include <TinyGPS++.h>
#include <SoftwareSerial.h>

static const int RXPin = 10, TXPin = 11;
static const uint32_t SIOBaud = 9600;
static const uint32_t GPSBaud = 9600;

int pulsePin = A0;       // Pulse Sensor가 연결된 핀
int signal;              // 측정된 신호 값
int threshold = 550;     // 박동 감지 임계값 (상황에 따라 조정 필요)
int gaspin = A1;

unsigned long lastBeatTime = 0;

TinyGPSPlus gps;
SoftwareSerial ss(RXPin, TXPin);

int beatCount = 0;
int bpm = 0;

void setup() {
  Serial.begin(9600);
  ss.begin(GPSBaud);
  pinMode(pulsePin, INPUT);
  pinMode(gaspin,INPUT);
}

void loop() {
  signal = analogRead(pulsePin);
  
printFloat(gps.location.lat(), gps.location.isValid(), 11, 6);
  printFloat(gps.location.lng(), gps.location.isValid(), 12, 6);

  Serial.println();

  smartDelay(1000);

  // 박동 감지: threshold 초과 + 최소 간격(300ms) 유지
  if (signal > threshold && millis() - lastBeatTime > 300) {
    lastBeatTime = millis();
    beatCount++;
    Serial.println("70");
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
  Serial.println(analogRead(gaspin));
  delay(20);
}

static void printFloat(float val, bool valid, int len, int prec)
{
  if (!valid)
  {
    while (len-- > 1)
      Serial.print('*');
    Serial.print(' ');
  }
  else
  {
    Serial.print(val, prec);
    int vi = abs((int)val);
    int flen = prec + (val < 0.0 ? 2 : 1); // . and -
    flen += vi >= 1000 ? 4 : vi >= 100 ? 3 : vi >= 10 ? 2 : 1;
    for (int i = flen; i < len; ++i)
      Serial.print(' ');
  }
  smartDelay(0);
}

static void printInt(unsigned long val, bool valid, int len)
{
  char sz[32] = "*****************";
  if (valid)
  {
    sprintf(sz, "%ld", val);
    sz[len] = 0;
    for (int i = strlen(sz); i < len; ++i)
      sz[i] = ' ';
    if (len > 0)
      sz[len - 1] = ' ';
    Serial.print(sz);
  }
  smartDelay(0);
}
static void smartDelay(unsigned long ms)
{
  unsigned long start = millis();
  do {
    while (ss.available())
      gps.encode(ss.read());
  } while (millis() - start < ms);
}
