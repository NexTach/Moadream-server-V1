import hashlib
import random
from datetime import datetime, timedelta
from flask import Flask, request, jsonify

app = Flask(__name__)


def get_customer_seed(customer_id, utility_type):
    seed_str = f"{customer_id}_{utility_type}"
    return int(hashlib.md5(seed_str.encode()).hexdigest(), 16) % (10**8)


def generate_electricity_usage(customer_id, measured_at):
    random.seed(get_customer_seed(customer_id, "electricity") + measured_at.hour)

    hour = measured_at.hour
    base_usage = 0.0

    if 6 <= hour < 9:
        base_usage = random.uniform(1.2, 2.5)
    elif 9 <= hour < 18:
        base_usage = random.uniform(0.3, 0.8)
    elif 18 <= hour < 23:
        base_usage = random.uniform(1.5, 3.0)
    else:
        base_usage = random.uniform(0.2, 0.5)

    month = measured_at.month
    if month in [6, 7, 8]:
        base_usage *= random.uniform(1.5, 2.0)
    elif month in [12, 1, 2]:
        base_usage *= random.uniform(1.3, 1.7)

    unit_price = 0.0
    if base_usage <= 1.0:
        unit_price = 93.3
    elif base_usage <= 2.0:
        unit_price = 187.9
    else:
        unit_price = 280.6

    charge = base_usage * unit_price

    return round(base_usage, 2), round(charge, 2)


def generate_water_usage(customer_id, measured_at):
    random.seed(get_customer_seed(customer_id, "water") + measured_at.hour)

    hour = measured_at.hour
    base_usage = 0.0

    if 6 <= hour < 9:
        base_usage = random.uniform(0.08, 0.15)
    elif 9 <= hour < 18:
        base_usage = random.uniform(0.01, 0.03)
    elif 19 <= hour < 23:
        base_usage = random.uniform(0.10, 0.20)
    else:
        base_usage = random.uniform(0.005, 0.015)

    if measured_at.weekday() >= 5:
        base_usage *= random.uniform(1.2, 1.5)

    unit_price = 580
    charge = base_usage * unit_price

    return round(base_usage, 3), round(charge, 2)


def generate_gas_usage(customer_id, measured_at):
    random.seed(get_customer_seed(customer_id, "gas") + measured_at.hour)

    hour = measured_at.hour
    month = measured_at.month
    base_usage = 0.0

    if 6 <= hour < 9:
        base_usage = random.uniform(0.15, 0.30)
    elif 18 <= hour < 22:
        base_usage = random.uniform(0.20, 0.40)
    else:
        base_usage = random.uniform(0.05, 0.10)

    if month in [11, 12, 1, 2, 3]:
        base_usage *= random.uniform(2.0, 3.5)
    elif month in [4, 5, 9, 10]:
        base_usage *= random.uniform(0.8, 1.2)
    else:
        base_usage *= random.uniform(0.5, 0.8)

    unit_price = 650
    charge = base_usage * unit_price

    return round(base_usage, 3), round(charge, 2)


@app.route("/api/usage", methods=["POST"])
def get_usage():
    data = request.json
    customer_id = data.get("customerId")
    utility_type = data.get("utilityType")
    start_date_str = data.get("startDate")
    end_date_str = data.get("endDate")

    if not all([customer_id, utility_type, start_date_str, end_date_str]):
        return jsonify({"status": "FAILED", "message": "필수 파라미터가 누락되었습니다."}), 400

    start_date = datetime.fromisoformat(start_date_str.replace("Z", "+00:00"))
    end_date = datetime.fromisoformat(end_date_str.replace("Z", "+00:00"))

    records = []
    current = start_date

    while current <= end_date:
        usage_amount = 0.0
        charge = 0.0
        unit = ""

        if utility_type == "ELECTRICITY":
            usage_amount, charge = generate_electricity_usage(customer_id, current)
            unit = "kWh"
        elif utility_type == "WATER":
            usage_amount, charge = generate_water_usage(customer_id, current)
            unit = "m³"
        elif utility_type == "GAS":
            usage_amount, charge = generate_gas_usage(customer_id, current)
            unit = "m³"
        else:
            return jsonify({"status": "FAILED", "message": f"지원하지 않는 유형입니다: {utility_type}"}), 400

        records.append({"usageAmount": usage_amount, "unit": unit, "charge": charge, "measuredAt": current.isoformat()})

        current += timedelta(hours=1)

    return jsonify(
        {
            "customerId": customer_id,
            "utilityType": utility_type,
            "records": records,
            "status": "SUCCESS",
            "message": "사용량 조회 성공",
        }
    )


@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({"status": "UP", "service": "Mock Utility API", "timestamp": datetime.now().isoformat()})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=9000, debug=True)
