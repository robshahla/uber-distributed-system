FROM python:3

WORKDIR /usr/server/
COPY ZKInit.py .
RUN pip install kazoo
ENTRYPOINT ["python3", "ZKInit.py"]
CMD ["zookeeper-address"]