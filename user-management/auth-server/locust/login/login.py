import random

from locust import task, FastHttpUser, between


class Test(FastHttpUser):
    wait_time = between(1, 3)
    connection_timeout = 30.0
    network_timeout = 30.0

    @task
    def login(self):
        email = f"dummy{random.randint(1, 1001)}@example.com"
        password = "Test1234!"
        json = {"email": email, "password": password}

        response = self.client.post("/auth-server/login", json=json)

        if response.status_code != 200:
            print(f"Login failed. {response.text}")
