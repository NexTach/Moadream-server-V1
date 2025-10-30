import pytest
from datetime import datetime
from app import app, get_customer_seed, generate_electricity_usage, generate_water_usage, generate_gas_usage


@pytest.fixture
def client():
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client


class TestCustomerSeed:
    def test_get_customer_seed_consistency(self):
        """같은 customer_id와 utility_type은 항상 같은 seed를 반환해야 함"""
        seed1 = get_customer_seed("CUST001", "electricity")
        seed2 = get_customer_seed("CUST001", "electricity")
        assert seed1 == seed2

    def test_get_customer_seed_different_customer(self):
        """다른 customer_id는 다른 seed를 반환해야 함"""
        seed1 = get_customer_seed("CUST001", "electricity")
        seed2 = get_customer_seed("CUST002", "electricity")
        assert seed1 != seed2

    def test_get_customer_seed_different_utility(self):
        """다른 utility_type은 다른 seed를 반환해야 함"""
        seed1 = get_customer_seed("CUST001", "electricity")
        seed2 = get_customer_seed("CUST001", "water")
        assert seed1 != seed2


class TestUsageGeneration:
    def test_generate_electricity_usage_returns_tuple(self):
        """전기 사용량 생성은 (사용량, 요금) 튜플을 반환해야 함"""
        measured_at = datetime(2024, 1, 15, 12, 0)
        usage, charge = generate_electricity_usage("CUST001", measured_at)
        assert isinstance(usage, float)
        assert isinstance(charge, float)
        assert usage > 0
        assert charge > 0

    def test_generate_water_usage_returns_tuple(self):
        """수도 사용량 생성은 (사용량, 요금) 튜플을 반환해야 함"""
        measured_at = datetime(2024, 1, 15, 12, 0)
        usage, charge = generate_water_usage("CUST001", measured_at)
        assert isinstance(usage, float)
        assert isinstance(charge, float)
        assert usage > 0
        assert charge > 0

    def test_generate_gas_usage_returns_tuple(self):
        """가스 사용량 생성은 (사용량, 요금) 튜플을 반환해야 함"""
        measured_at = datetime(2024, 1, 15, 12, 0)
        usage, charge = generate_gas_usage("CUST001", measured_at)
        assert isinstance(usage, float)
        assert isinstance(charge, float)
        assert usage > 0
        assert charge > 0

    def test_usage_consistency(self):
        """같은 customer_id와 시간은 항상 같은 사용량을 반환해야 함"""
        measured_at = datetime(2024, 1, 15, 12, 0)
        usage1, charge1 = generate_electricity_usage("CUST001", measured_at)
        usage2, charge2 = generate_electricity_usage("CUST001", measured_at)
        assert usage1 == usage2
        assert charge1 == charge2


class TestHealthEndpoint:
    def test_health_check(self, client):
        """헬스 체크 엔드포인트는 200을 반환해야 함"""
        response = client.get('/health')
        assert response.status_code == 200
        data = response.get_json()
        assert data['status'] == 'UP'
        assert data['service'] == 'Mock Utility API'
        assert 'timestamp' in data


class TestUsageEndpoint:
    def test_get_usage_success_electricity(self, client):
        """전기 사용량 조회 성공"""
        payload = {
            'customerId': 'CUST001',
            'utilityType': 'ELECTRICITY',
            'startDate': '2024-01-15T00:00:00Z',
            'endDate': '2024-01-15T02:00:00Z'
        }
        response = client.post('/api/usage', json=payload)
        assert response.status_code == 200
        data = response.get_json()
        assert data['status'] == 'SUCCESS'
        assert data['customerId'] == 'CUST001'
        assert data['utilityType'] == 'ELECTRICITY'
        assert len(data['records']) == 3  # 3시간 데이터
        for record in data['records']:
            assert 'usageAmount' in record
            assert 'unit' in record
            assert 'charge' in record
            assert 'measuredAt' in record
            assert record['unit'] == 'kWh'

    def test_get_usage_success_water(self, client):
        """수도 사용량 조회 성공"""
        payload = {
            'customerId': 'CUST001',
            'utilityType': 'WATER',
            'startDate': '2024-01-15T00:00:00Z',
            'endDate': '2024-01-15T01:00:00Z'
        }
        response = client.post('/api/usage', json=payload)
        assert response.status_code == 200
        data = response.get_json()
        assert data['status'] == 'SUCCESS'
        assert data['utilityType'] == 'WATER'
        assert data['records'][0]['unit'] == 'm³'

    def test_get_usage_success_gas(self, client):
        """가스 사용량 조회 성공"""
        payload = {
            'customerId': 'CUST001',
            'utilityType': 'GAS',
            'startDate': '2024-01-15T00:00:00Z',
            'endDate': '2024-01-15T01:00:00Z'
        }
        response = client.post('/api/usage', json=payload)
        assert response.status_code == 200
        data = response.get_json()
        assert data['status'] == 'SUCCESS'
        assert data['utilityType'] == 'GAS'
        assert data['records'][0]['unit'] == 'm³'

    def test_get_usage_missing_parameters(self, client):
        """필수 파라미터 누락 시 400 에러"""
        payload = {
            'customerId': 'CUST001',
            'utilityType': 'ELECTRICITY'
        }
        response = client.post('/api/usage', json=payload)
        assert response.status_code == 400
        data = response.get_json()
        assert data['status'] == 'FAILED'

    def test_get_usage_invalid_utility_type(self, client):
        """지원하지 않는 유틸리티 타입"""
        payload = {
            'customerId': 'CUST001',
            'utilityType': 'INVALID',
            'startDate': '2024-01-15T00:00:00Z',
            'endDate': '2024-01-15T01:00:00Z'
        }
        response = client.post('/api/usage', json=payload)
        assert response.status_code == 400
        data = response.get_json()
        assert data['status'] == 'FAILED'
        assert 'INVALID' in data['message']

    def test_get_usage_consistency(self, client):
        """같은 요청은 같은 결과를 반환해야 함"""
        payload = {
            'customerId': 'CUST001',
            'utilityType': 'ELECTRICITY',
            'startDate': '2024-01-15T12:00:00Z',
            'endDate': '2024-01-15T13:00:00Z'
        }
        response1 = client.post('/api/usage', json=payload)
        response2 = client.post('/api/usage', json=payload)
        data1 = response1.get_json()
        data2 = response2.get_json()
        assert data1['records'] == data2['records']