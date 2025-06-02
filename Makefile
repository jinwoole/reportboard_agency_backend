IMAGE_NAME := ghcr.io/jinwoole/worklog
IMAGE_TAG := 0.1

.PHONY: all build push

all: build

build:
	podman build -t $(IMAGE_NAME):$(IMAGE_TAG) .

push:
	podman push $(IMAGE_NAME):$(IMAGE_TAG)
