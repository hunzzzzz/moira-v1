import random

from locust import task, FastHttpUser, between


class Test(FastHttpUser):
    wait_time = between(1, 5)
    connection_timeout = 30.0
    network_timeout = 30.0

    @task
    def first_page(self):
        my_id = "5904b7a6-6ebb-4a18-a149-1a7c181763c1"
        locust_authorization = f"Locust {my_id}"

        response = self.client.get(url=f"/relation-server/users/{my_id}/followings",
                                   headers={"Authorization": f"{locust_authorization}"})

        if response.status_code != 200:
            print(f"Get Followings failed. {response.text}")

    @task
    def second_page(self):
        my_id = "5904b7a6-6ebb-4a18-a149-1a7c181763c1"
        cursor = "9c8152a8-1421-41c5-84e7-96fb672bd60e"
        locust_authorization = f"Locust {my_id}"

        response = self.client.get(url=f"/relation-server/users/{my_id}/followings?cursor={cursor}",
                                   headers={"Authorization": f"{locust_authorization}"})

        if response.status_code != 200:
            print(f"Get Followings failed. {response.text}")
