from locust import task, FastHttpUser, between
import random

class Test(FastHttpUser):
    wait_time = between(1, 3)
    connection_timeout = 30.0
    network_timeout = 30.0
    my_atk = None

    def on_start(self):
        response = self.client.post("/login", json={"email": "moira_admin@gmail.com", "password": "Admin1234!"})
        if response.status_code == 200:
            self.my_atk = response.json().get("atk")
        else:
            print("Admin login failed.")

    @task(5)
    def load_feed(self):
        if self.my_atk:
            self.client.get("/posts", headers={"Authorization": f"{self.my_atk}"})
        else:
            print("ATK is not available")

    @task(5)
    def create_post(self):
        random_number = random.randint(1, 10000)
        email = f"dummy{random_number}@gmail.com"

        response = self.client.post("/login", json={"email": email, "password": "Dummy1234!"})
        if response.status_code == 200:
            atk = response.json().get("atk")
            if atk:
                self.client.post("/posts", headers={"Authorization": f"{atk}"}, json={"content": "테스트 게시글", "scope": "PUBLIC"})
            else:
                print(f"Dummy user login failed (no ATK): {response.text}")
        else:
            print(f"Dummy user login failed: {response.status_code} {response.text}")