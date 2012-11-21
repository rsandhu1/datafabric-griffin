#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
This module is intended to provide a simple acceptance test automation
against the Griffin GridFTP server.

The following requirements need to be met:

 * Currently valid Grid proxy certificate available.
 * Availability of UberFTP in the PATH of the user executing this.
"""

## Created: 2010-12-15 Guy K. Kloss <Guy.Kloss@aut.ac.nz>
##
## Copyright 2010 Australian Research Collaboration Service
##                and Auckland University of Technology, New Zealand
##
## Some rights reserved.
##
## http://www.arcs.org.au/
## http://www.aut.ac.nz/

import os
import doctest
import unittest
import test_config

def execute(command):
    """
    Executes the command given in a shell, and returns all output from
    STDIN and STDERR as a string.

    @param command: Command to execute (incl. calling parameters).
    @type command: C{string}
    """
    prepared_command = command % test_config.REPLACEMENTS
    try:
        return _execute_subprocess(prepared_command)
    except ImportError:
        return _execute_popen(prepared_command)


def _execute_subprocess(command):    
    """
    Executes using the (newer) preferred subprocess module.
    """
    import subprocess
    the_process = subprocess.Popen([command],
                                   shell=True,
                                   stdout=subprocess.PIPE,
                                   stderr=subprocess.STDOUT,
                                   close_fds=True)
    return ''.join(the_process.stdout.readlines())


def _execute_popen(command):
    """
    Executes using the (older) now deprecated os.popen style.
    """
    (_, child_stdout_and_stderr) = os.popen4(command)
    return ''.join(child_stdout_and_stderr.readlines())


def _find_test_files():
    """
    Finds all test files (.txt files in the TESTS_DIR), and returns
    them as a list.
    """
    text_files = [x
                  for x in os.listdir(test_config.TESTS_DIR)
                  if x.endswith('.txt')]
    test_files = [os.path.join(test_config.TESTS_DIR, x)
                  for x in text_files]
    return test_files


def _test(test_runner):
    doctest.testmod()
    test_suite = doctest.DocFileSuite(*_find_test_files())
    test_runner.run(test_suite)
    

if __name__ == '__main__':
    test_runner = unittest.TextTestRunner(verbosity=2)
    _test(test_runner)
