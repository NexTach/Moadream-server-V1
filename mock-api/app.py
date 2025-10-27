from flask import Flask, request, jsonify
from datetime import datetime, timedelta
import random
import hashlib

app = Flask(__name__)

# 고객 ID 기반 시드 생성
def get_customer_seed(customer_id, utility_type):
    seed_str = f"{customer_id}_{utility_type}"
    return int(hashlib.md5(seed_str.encode()).hexdigest(), 16) % (10 ** 8)

# 시간대별 전기 사용 패턴 (kWh)
def generate_electricity_usage(customer_id, measured_at):
    random.seed(get_customer_seed(customer_id, 'electricity') + measured_at.hour)

    hour = measured_at.hour
    base_usage = 0.0

    # 시간대별 패턴: 오전 6-9시, 저녁 18-23시에 사용량 증가
    if 6 <= hour < 9:
        base_usage = random.uniform(1.2, 2.5)  # 아침 준비
    elif 9 <= hour < 18:
        base_usage = random.uniform(0.3, 0.8)  # 외출 중 대기전력
    elif 18 <= hour < 23:
        base_usage = random.uniform(1.5, 3.0)  # 저녁 활동
    else:
        base_usage = random.uniform(0.2, 0.5)  # 수면 중

    # 계절 보정 (여름/겨울 에어컨/난방)
    month = measured_at.month
    if month in [6, 7, 8]:  # 여름
        base_usage *= random.uniform(1.5, 2.0)
    elif month in [12, 1, 2]:  # 겨울
        base_usage *= random.uniform(1.3, 1.7)

    # 요금 계산 (누진제 단순화)
    unit_price = 0.0
    if base_usage <= 1.0:
        unit_price = 93.3
    elif base_usage <= 2.0:
        unit_price = 187.9
    else:
        unit_price = 280.6

    charge = base_usage * unit_price

    return round(base_usage, 2), round(charge, 2)

# 수도 사용 패턴 (m³)
def generate_water_usage(customer_id, measured_at):
    random.seed(get_customer_seed(customer_id, 'water') + measured_at.hour)

    hour = measured_at.hour
    base_usage = 0.0

    # 시간대별 패턴: 아침 6-9시, 저녁 19-23시 사용 증가
    if 6 <= hour < 9:
        base_usage = random.uniform(0.08, 0.15)  # 세면, 샤워
    elif 9 <= hour < 18:
        base_usage = random.uniform(0.01, 0.03)  # 낮 최소 사용
    elif 19 <= hour < 23:
        base_usage = random.uniform(0.10, 0.20)  # 요리, 설거지, 샤워
    else:
        base_usage = random.uniform(0.005, 0.015)  # 밤 최소 사용

    # 주말 보정
    if measured_at.weekday() >= 5:  # 토, 일
        base_usage *= random.uniform(1.2, 1.5)

    # 요금 계산 (m³당 약 580원)
    unit_price = 580
    charge = base_usage * unit_price

    return round(base_usage, 3), round(charge, 2)

# 가스 사용 패턴 (m³)
def generate_gas_usage(customer_id, measured_at):
    random.seed(get_customer_seed(customer_id, 'gas') + measured_at.hour)

    hour = measured_at.hour
    month = measured_at.month
    base_usage = 0.0

    # 시간대별 패턴: 아침 6-9시, 저녁 18-22시 사용 (요리, 난방)
    if 6 <= hour < 9:
        base_usage = random.uniform(0.15, 0.30)  # 아침 식사
    elif 18 <= hour < 22:
        base_usage = random.uniform(0.20, 0.40)  # 저녁 식사 및 난방
    else:
        base_usage = random.uniform(0.05, 0.10)  # 최소 사용

    # 계절 보정 (겨울철 난방)
    if month in [11, 12, 1, 2, 3]:  # 겨울
        base_usage *= random.uniform(2.0, 3.5)
    elif month in [4, 5, 9, 10]:  # 봄/가을
        base_usage *= random.uniform(0.8, 1.2)
    else:  # 여름
        base_usage *= random.uniform(0.5, 0.8)

    # 요금 계산 (m³당 약 650원)
    unit_price = 650
    charge = base_usage * unit_price

    return round(base_usage, 3), round(charge, 2)

# 전기 사용량 API
@app.route('/api/usage', methods=['POST'])
def get_usage():
    data = request.json
    customer_id = data.get('customerId')
    utility_type = data.get('utilityType')
    start_date = datetime.fromisoformat(data.get('startDate').replace('Z', '+00:00'))
    end_date = datetime.fromisoformat(data.get('endDate').replace('Z', '+00:00'))

    if not all([customer_id, utility_type, start_date, end_date]):
        return jsonify({
            'status': 'FAILED',
            'message': '필수 파라미터가 누락되었습니다.'
        }), 400

    records = []
    current = start_date

    while current <= end_date:
        usage_amount = 0.0
        charge = 0.0
        unit = ''

        if utility_type == 'ELECTRICITY':
            usage_amount, charge = generate_electricity_usage(customer_id, current)
            unit = 'kWh'
        elif utility_type == 'WATER':
            usage_amount, charge = generate_water_usage(customer_id, current)
            unit = 'm³'
        elif utility_type == 'GAS':
            usage_amount, charge = generate_gas_usage(customer_id, current)
            unit = 'm³'
        else:
            return jsonify({
                'status': 'FAILED',
                'message': f'지원하지 않는 유형입니다: {utility_type}'
            }), 400

        records.append({
            'usageAmount': usage_amount,
            'unit': unit,
            'charge': charge,
            'measuredAt': current.isoformat()
        })

        current += timedelta(hours=1)

    return jsonify({
        'customerId': customer_id,
        'utilityType': utility_type,
        'records': records,
        'status': 'SUCCESS',
        'message': '사용량 조회 성공'
    })

# 헬스체크
@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({
        'status': 'UP',
        'service': 'Mock Utility API',
        'timestamp': datetime.now().isoformat()
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=9000, debug=True)