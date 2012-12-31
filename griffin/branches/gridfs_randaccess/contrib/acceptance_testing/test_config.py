# -*- coding: utf-8 -*-
"""
Configuration of some parameters for the tests.
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

from acceptance_testing import execute
TESTS_DIR = 'tests'

# The "execute()" statement tries to determine reliably the FQDN of this machine.
# Alternatively, it can also be set fully manually.
REPLACEMENTS = {'host': execute('host -TtA $(hostname -s)', replacements=False).split()[0],
                'port': '2810'}
