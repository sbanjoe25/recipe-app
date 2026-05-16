RUN_MAVEN = sh mvnw
APP_IMAGE = recipe-app
APP_PORT ?= 8080
APP_VERSION ?= latest

.env:
	cp -f .env.example .env

test:
	$(RUN_MAVEN) test
	$(RUN_MAVEN) jacoco:report

compile:
	$(RUN_MAVEN) compile -DskipTests=true

build:
	$(RUN_MAVEN) compile jib:dockerBuild -DskipTests=true

logs:
	docker logs -f $(APP_IMAGE)

run:
	docker run --rm --detach -p $(APP_PORT):8080 --env-file .env --network $(NETWORK_NAME) --name $(APP_IMAGE) $(APP_IMAGE):latest
	$(MAKE) logs

stop:
	@docker rm -f $(APP_IMAGE)

start: build run

restart: stop run
