// Cross-platform backend launcher: loads backend/.env into the environment,
// then runs the Maven wrapper's spring-boot:run. Used by `npm start` so it
// behaves the same whether npm dispatches scripts through cmd.exe or a POSIX shell.
import { spawn } from "node:child_process";
import { readFileSync, existsSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const root = dirname(dirname(fileURLToPath(import.meta.url)));
const backendDir = join(root, "backend");
const envFile = join(backendDir, ".env");

const env = { ...process.env };
if (existsSync(envFile)) {
  for (const raw of readFileSync(envFile, "utf8").split(/\r?\n/)) {
    const line = raw.trim();
    if (!line || line.startsWith("#")) continue;
    const eq = line.indexOf("=");
    if (eq === -1) continue;
    const key = line.slice(0, eq).trim();
    let value = line.slice(eq + 1).trim();
    // Strip a single layer of surrounding quotes if present.
    if (value.length >= 2 && ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'")))) {
      value = value.slice(1, -1);
    }
    env[key] = value;
  }
} else {
  console.warn("No backend/.env found — starting with current environment only.");
}

const isWin = process.platform === "win32";
// Use an absolute path to the wrapper: with shell:true on Windows, cmd.exe does
// not resolve a bare script name from cwd, so mvnw.cmd would not be found.
const wrapper = join(backendDir, isWin ? "mvnw.cmd" : "mvnw");

const child = spawn(wrapper, ["spring-boot:run"], {
  cwd: backendDir,
  env,
  stdio: "inherit",
  shell: isWin, // .cmd needs a shell on Windows
});

child.on("exit", (code) => process.exit(code ?? 0));
child.on("error", (err) => {
  console.error("Failed to start backend:", err.message);
  process.exit(1);
});
