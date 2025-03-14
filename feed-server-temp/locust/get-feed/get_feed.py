# import random
#
# from locust import task, FastHttpUser, between
#
#
# class Test(FastHttpUser):
#     wait_time = between(1, 5)
#     connection_timeout = 30.0
#     network_timeout = 30.0
#
#     @task
#     def first_page(self):
#         my_id = "9b8d5488-a3aa-4e99-9d0a-66df3a81a546"
#         locust_authorization = f"Locust {my_id}"
#         cursor = ""
#
#         while True:
#             url = f"/feed-server/feed"
#             if cursor:
#                 url += f"?cursor={cursor}"
#
#             response = self.client.get(url=url, headers={"Authorization": f"{locust_authorization}"})
#
#             if response.status_code != 200:
#                 print(f"Get Comments failed. {response.text}")
#             else:
#                 data = response.json()
#                 next_cursor = data.get("nextCursor")
#
#                 if next_cursor is None:
#                     break
#
#                 cursor = next_cursor

import time

from locust import task, FastHttpUser, between, run_single_user


class Test(FastHttpUser):
    wait_time = between(1, 5)

    def on_start(self):
        self.user_id = "9b8d5488-a3aa-4e99-9d0a-66df3a81a546"  # 사용자 ID
        self.locust_authorization = f"Locust {self.user_id}"
        self.start_time = time.time()  # 시작 시간 측정

    @task
    def first_page(self):
        cursor = ""
        while True:
            url = f"/feed-server/feed"
            if cursor:
                url += f"?cursor={cursor}"

            response = self.client.get(url=url, headers={"Authorization": f"{self.locust_authorization}"})

            if response.status_code != 200:
                print(f"Error: {response.text}")
                break
            else:
                data = response.json()
                next_cursor = data.get("nextCursor")

                if next_cursor is None:
                    break

                cursor = next_cursor

        end_time = time.time()  # 종료 시간 측정
        elapsed_time = (end_time - self.start_time)  # 경과 시간 계산 (초 단위)
        print(f"Elapsed time: {elapsed_time} seconds")  # 수행 시간 출력


if __name__ == "__main__":
    run_single_user(Test)
