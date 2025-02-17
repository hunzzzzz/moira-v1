import random

from locust import task, FastHttpUser, between


class Test(FastHttpUser):
    wait_time = between(1, 5)
    connection_timeout = 30.0
    network_timeout = 30.0

    @task
    def first_page(self):
        my_id = "ef9ab91a-4b19-4fd1-9e77-4ac90727ae1b"
        locust_authorization = f"Locust {my_id}"
        cursor = ""

        while True:
            url = f"/relation-server/users/{my_id}/followings"
            if cursor:
                url += f"?cursor={cursor}"

            response = self.client.get(url=url, headers={"Authorization": f"{locust_authorization}"})

            if response.status_code != 200:
                print(f"Get Followings failed. {response.text}")
            else:
                data = response.json()
                next_cursor = data.get("nextCursor")

                if next_cursor is None:
                    break

                cursor = next_cursor
