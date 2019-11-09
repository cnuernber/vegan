SOURCEDIR := "src"
BUILD_SOURCES := project.clj Makefile node/package.json
CLJS_SOURCES := $(shell find $(SOURCEDIR) -name '*.cljs')
CLJC_SOURCES := $(shell find $(SOURCEDIR) -name '*.cljc')
MODULEDIR := $(shell pwd)/node_modules
VEGAN := $(MODULEDIR)/bin/vegan
JSON_RESOURCES := $(shell find resources -name '*.json')

all: $(VEGAN)

node/app.js: $(CLJS_SOURCES) $(CLJC_SOURCES) $(BUILD_SOURCES) $(JSON_RESOURCES)
	rm -f node/app.js
	lein cljsbuild once
	cp $(JSON_RESOURCES) node/out/vegan

$(VEGAN): node/app.js
	cd node && NPM_CONFIG_PREFIX=$(MODULEDIR) npm link . && cd ..

clean:
	rm -rf node/app.js node/out $(VEGAN)
