SHELL := /bin/bash
CMD := 
ONOS_ROOT := $(shell pwd)
BASH_SET := $(shell bash -c "ONOS_ROOT=$(ONOS_ROOT) && source $(ONOS_ROOT)/tools/dev/bash_profile;$(CMD)")

.PHONY: check
check:
	$(shell bash -c "ONOS_ROOT=$(ONOS_ROOT) && source $(ONOS_ROOT)/tools/dev/bash_profile;$(ONOS_ROOT)/tools/build/onos-buck test>&2;")

.PHONY: build
build:
	$(shell bash -c "ONOS_ROOT=$(ONOS_ROOT); \
	 source $(ONOS_ROOT)/tools/dev/bash_profile; \
	  onos-package>&2; \
	  ")
	

.PHONY: run
run:
	$(shell bash -c "ONOS_ROOT=$(ONOS_ROOT); \
	 source $(ONOS_ROOT)/tools/dev/bash_profile; \
	  NO_BUCKD=1 onos-buck run onos-local -- debug >&2; \
	  ")

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