SHELL := /bin/bash
ONOS_ROOT := $(shell pwd)
.PHONY: path
path:
	export ONOS_ROOT=$(ONOS_ROOT)

.PHONY: config
config: path
	source $$ONOS_ROOT/tools/dev/bash_profile

.PHONY: check
check: config
	$$ONOS_ROOT/tools/build/onos-buck test

.PHONY: build
build: config
	onos-package

.PHONY: run
run:
	NO_BUCKD=1 onos-buck run onos-local -- debug

.PHONY: web
web:
	$$ONOS_ROOT/tools/test/bin/onos-gui localhost

.PHONY: cli
cli:
	$$ONOS_ROOT/tools/test/bin/onos localhost

.PHONY: check_bazel
check_bazel:
	@which bazel > /dev/null; if [ $$? -ne 0 ]; then \
		echo "Please install bazel"; \
		echo "See the detail: https://docs.bazel.build/versions/master/install.html"; \
		exit 1; \
	fi