from locust import task, FastHttpUser
import random

class Test(FastHttpUser):
    connection_timeout = 30.0
    network_timeout = 30.0

    @task
    def first_page(self):
        user_id = "5d73df04-efc2-4ddf-8366-9fe89236c59d"
        self.client.get(url=f"/test/users/{user_id}/followings")

    @task
    def second_page(self):
        user_id = "5d73df04-efc2-4ddf-8366-9fe89236c59d"
        cursor = "2025-02-02T15:27:31.899874"
        self.client.get(url=f"/test/users/{user_id}/followings?cursor={cursor}")