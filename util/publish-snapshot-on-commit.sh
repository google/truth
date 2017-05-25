# see https://coderwall.com/p/9b_lfq

if [ "$TRAVIS_REPO_SLUG" == "google/truth" ] && \
   [ "$TRAVIS_JDK_VERSION" == "$JDK_FOR_PUBLISHING" ] && \
   [ "$TRAVIS_PULL_REQUEST" == "false" ] && \
   [ "$TRAVIS_BRANCH" == "master" ]; then
  echo -e "Publishing maven snapshot...\n"

  mvn clean deploy --settings="util/settings.xml" -DskipTests=true -Dmaven.javadoc.skip=true

  echo -e "Published maven snapshot"
fi
