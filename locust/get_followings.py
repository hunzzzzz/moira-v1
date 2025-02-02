from locust import task, FastHttpUser
import random

class Test(FastHttpUser):
    wait_time = between(1, 3)
    connection_timeout = 30.0
    network_timeout = 30.0

    @task
    def first_page(self):
        user_id = "5d73df04-efc2-4ddf-8366-9fe89236c59d"
        self.client.get(url=f"/users/{user_id}/followings", headers={"Authorization": "locust"})

    @task
    def second_page(self):
        user_id = "5d73df04-efc2-4ddf-8366-9fe89236c59d"
        cursor = "ac34fd3b-307b-469b-8b89-eee52212bfd8"
        self.client.get(url=f"/users/{user_id}/followings?cursor={cursor}", headers={"Authorization": "locust"})