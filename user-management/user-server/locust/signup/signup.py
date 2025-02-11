from locust import task, FastHttpUser, between
import uuid

class Test(FastHttpUser):
    wait_time = between(1, 3)
    connection_timeout = 30.0
    network_timeout = 30.0

    @task
    def signup(self):
        random_num = uuid.uuid4()
        json={"email": f"{random_num}@example.com", "password":"Temp1234!", "password2": "Temp1234!", "name": "테스트"}
        response = self.client.post("/signup", json=json)

        if response.status_code != 201:
            print(f"Signup failed. {response.text}")