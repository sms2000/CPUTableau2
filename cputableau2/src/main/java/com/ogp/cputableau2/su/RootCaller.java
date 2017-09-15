package com.ogp.cputableau2.su;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.ogp.cputableau2.global.Constants;
import com.ogp.cputableau2.results.RPCResult;
import com.ogp.cputableau2.servlets.ExecuteWithTimeout;


public class RootCaller {
    private static final String CMD_SU = "su";
    private static final String[] SU_PATHES = {"/system/xbin/su", "/system/bin/su", "/sbin/su", "/sbin/magisk/su"};

    private static RootExecutor rootExecutor = null;
    private static int createdCounter = 0;

    public enum RootStatus {NO_ROOT, ROOT_FAILED, ROOT_GRANTED}


    public static class RootExecutor extends ExecuteWithTimeout {
        private static String COMMAND_ANSWER_END = "%d_FINISH_%d";
        private static String COMMAND_TAIL;
        private Process chperm;
        private BufferedReader reader = null;
        private BufferedWriter writer = null;


        private static class ExecuteOnRoot extends ExecuteParams {
            private String command;

            ExecuteOnRoot(final String command) {
                this.command = command;
            }
        }


        static {
            long random = Math.abs(new Random().nextInt());
            COMMAND_ANSWER_END = String.format(Locale.US, COMMAND_ANSWER_END, random, random);
            COMMAND_TAIL = ";echo '\n" + COMMAND_ANSWER_END + "'\n";
            Log.i(Constants.TAG, "RootCaller::RootExecutor::<static>. Final random string: " + COMMAND_ANSWER_END);
        }

        RootExecutor() {
            super();

            initialize();

            Log.v(Constants.TAG, "RootCaller::RootExecutor::<init>. Entry/Exit.");
        }


        private synchronized boolean initialize() {
            Log.v(Constants.TAG, "RootCaller::RootExecutor::initialize. Entry...");

            try {
                chperm = Runtime.getRuntime().exec(CMD_SU);
                reader = new BufferedReader(new InputStreamReader(chperm.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(chperm.getOutputStream()));

                sleep(500);

                Log.i(Constants.TAG, "RootCaller::RootExecutor::initialize. Created a root process.");
            } catch (Exception ignored) {
                chperm = null;
                Log.e(Constants.TAG, "RootCaller::RootExecutor::initialize. Failed to create a root process.");
            }

            Log.v(Constants.TAG, "RootCaller::RootExecutor::initialize. Exit.");
            return procAlive();
        }


        private void destroyProcess() {
            try {
                chperm.destroy();
            } catch (Exception ignored) {
            }

            chperm = null;
        }

        public RPCResult executeOnRoot(String command) {
            Log.v(Constants.TAG, "RootCaller::RootExecutor::executeOnRoot. Entry...");
            Log.d(Constants.TAG, "RootCaller::RootExecutor::executeOnRoot. Running command: " + command);

            RPCResult output;

            try {
                command += COMMAND_TAIL;
                output = sinkRootCommand(command);
            } catch (Exception e) {
                Log.e(Constants.TAG, "RootCaller::RootExecutor::executeOnRoot. Exception: ", e);
                output = new RPCResult(e);
            }

            Log.v(Constants.TAG, "RootCaller::RootExecutor::executeOnRoot. Exit.");
            return output;
        }


        @Override
        public RPCResult executeWithResult(final ExecuteParams params) throws InvocationTargetException {
            Log.v(Constants.TAG, "RootCaller::RootExecutor::executeWithResult. Entry...");

            RPCResult result;
            if (params instanceof ExecuteOnRoot) {
                ExecuteOnRoot executeOnRoot = (ExecuteOnRoot) params;
                result = sinkRootCommand(executeOnRoot.command);
            } else {
                executedError(new InvalidParameterException("Bad parameters: " + params.getClass().getName()));
                result = new RPCResult(new InvalidParameterException(String.format("'%s' instead of 'ExecuteOnRoot'", params.getClass().getName())));
            }

            Log.v(Constants.TAG, "RootCaller::RootExecutor::executeWithResult. Exit.");
            return result;
        }


        @Override
        public void executedError(Exception e) {
            Log.v(Constants.TAG, "RootCaller::RootExecutor::executedError. Entry...");

            Log.w(Constants.TAG, String.format("RootCaller::RootExecutor::executedError. Exception happened [%s]. Reinitialize 'root' process.", e.getMessage()));

            Log.v(Constants.TAG, "RootCaller::RootExecutor::executedError. Exit.");
        }


        private synchronized RPCResult sinkRootCommand(String command) {
            RPCResult result;
            List<String> received = new ArrayList<>();

            try {
                boolean initNow = true;

                try {
                    chperm.exitValue();
                } catch (IllegalThreadStateException ignored) {
                    initNow = false;
                } catch (Exception ignored) {
                }

                if (initNow) {
                    initialize();
                }

                if (!procAlive()) {
                    terminateRootProcess(rootExecutor);
                    throw new Exception("Dead");
                }

                writer.write(command, 0, command.length());
                writer.flush();

                while (true) {
                    String string = reader.readLine();
                    if (null == string) {
                        continue;
                    } else if (string.equals(COMMAND_ANSWER_END)) {
                        break;
                    }

                    received.add(string);
                }

                Log.d(Constants.TAG, String.format("RootCaller::RootExecutor::sinkRootCommand. Output includes %d line(s).", received.size()));
                result = new RPCResult(received);
            } catch (Exception e) {
                Log.e(Constants.TAG, "RootCaller::RootExecutor::sinkRootCommand. Exception with: " + e.getMessage());
                e.printStackTrace();

                destroyProcess();
                executedError(new InvalidParameterException(e.getMessage()));
                result = new RPCResult(e);
            }

            return result;
        }


        private boolean procAlive() {
            try {
                chperm.exitValue();
            } catch (IllegalThreadStateException ignored) {
                return true;
            } catch (Exception ignored) {
            }

            return false;
        }
    }


    public static RootStatus ifRootAvailable() {
        Log.v(Constants.TAG, "MainActivity::ifRootAvailable. Entry...");

        RootStatus success = RootStatus.NO_ROOT;

        for (String whichPath : SU_PATHES) {
            try {
                File file = new File(whichPath);
                if (file.canExecute()) {
                    success = RootStatus.ROOT_GRANTED;
                    Log.i(Constants.TAG, String.format("MainActivity::ifRootAvailable. 'Root' exists here [%s].", whichPath));
                    break;
                }

                Log.i(Constants.TAG, String.format("MainActivity::ifRootAvailable. Here [%s] the 'Root' doesn't exist.", whichPath));
            } catch (Throwable t) {
                Log.e(Constants.TAG, "MainActivity::ifRootAvailable. Exception: ", t);
            }
        }

        Log.v(Constants.TAG, "MainActivity::ifRootAvailable. Exit.");
        return success;
    }


    public static synchronized RootExecutor createRootProcess() {
        if (null == rootExecutor || !rootExecutor.procAlive()) {
            try {
                if (null != rootExecutor) {
                    terminateRootProcess(rootExecutor);
                }

                rootExecutor = new RootExecutor();

                if (++createdCounter == 1) {
                    Log.w(Constants.TAG, "RootCaller::createRootProcess. New root executor created. First of the kind.");
                } else {
                    Log.w(Constants.TAG, String.format("RootCaller::createRootProcess. New root executor created. Instance counter: %d.", createdCounter));
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "RootCaller::createRootProcess. Exception: ", e);
                rootExecutor = null;
            }
        }

        return rootExecutor;
    }


    public static void terminateRootProcess(RootExecutor rootExecutor) {
        try {
            rootExecutor.chperm.destroy();
        } catch (Throwable ignored) {
        }
    }
}
