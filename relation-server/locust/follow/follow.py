import random
import uuid

from locust import task, FastHttpUser, between


class Test(FastHttpUser):
    wait_time = between(1, 3)
    connection_timeout = 30.0
    network_timeout = 30.0

    @task
    def login(self):
        my_id = uuid.uuid4()
        locust_authorization = f"Locust {my_id}"
        target_id = uuid.uuid4()

        response = self.client.get(
            f"/relation-server/users/target/{target_id}/follow",
            headers={"Authorization": locust_authorization}
        )

        if response.status_code != 200:
            print(f"Follow failed. {response.text}")
