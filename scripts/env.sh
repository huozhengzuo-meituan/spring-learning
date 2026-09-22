# Source this file from the workspace root: source scripts/env.sh
# Keep the JDK local to this workspace; do not change the user's global default.
if [ -x "$PWD/.tools/jdk-21/Contents/Home/bin/java" ]; then
  export JAVA_HOME="$PWD/.tools/jdk-21/Contents/Home"
elif [ -x "$PWD/.tools/jdk-21/bin/java" ]; then
  export JAVA_HOME="$PWD/.tools/jdk-21"
else
  printf '%s\n' 'No workspace-local JDK found. Configure JAVA_HOME to your installed JDK 21.' >&2
  return 1
fi
export PATH="$JAVA_HOME/bin:$PATH"
java -version
