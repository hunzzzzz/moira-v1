import random

from locust import task, FastHttpUser, between


class Test(FastHttpUser):
    wait_time = between(1, 5)
    connection_timeout = 30.0
    network_timeout = 30.0

    @task
    def first_page(self):
        my_id = "c7f045f3-ebaf-4a2a-9149-ec26d628abff"
        locust_authorization = f"Locust {my_id}"
        cursor = ""

        while True:
            url = f"/post-server/posts/1/comments"
            if cursor:
                url += f"?cursor={cursor}"

            response = self.client.get(url=url, headers={"Authorization": f"{locust_authorization}"})

            if response.status_code != 200:
                print(f"Get Comments failed. {response.text}")
            else:
                data = response.json()
                next_cursor = data.get("nextCursor")

                if next_cursor is None:
                    break

                cursor = next_cursor
