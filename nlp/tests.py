import unittest
from pcky import Production, Grammar, N, T, CKY

# --- test cases

class CkyTest(unittest.TestCase):
    def setUp(self):
        pass

    def __simpleGrammar(self):
        return Grammar([Production('A', T('a'), None,   1),
                        Production('B', T('b'), None,   1),
                        Production('C', N('A'), N('B'), 1),
                        Production('D', N('C'), None,   1),
                        Production('E', N('D'), None,   1),
                        Production('A', T('c'), None,   1),
                        Production('F', N('A'), None,   1),
                        Production('S', N('C'), N('C'), 1)])

    def __unaryGrammar(self):
        return Grammar([Production('A', T('a'), None,   1),
                        Production('A', T('b'), None,   1),
                        Production('A', T('c'), None,   1),
                        Production('B', N('A'), None,   1),
                        Production('C', N('B'), None,   1)])

    def test_productionEquality(self):
        p1 = Production('A', N('C'), N('D'), 1)
        p2 = Production('A', N('C'), N('D'), 0.5)
        p3 = Production('B', N('C'), N('D'), 1)
        self.assertTrue(p1 == p2)
        self.assertFalse(p1 == p3)

    def test_terminalProductions(self):
        g = self.__simpleGrammar()
        self.assertEqual(len(g.terminalProductions('a')), 1)

    def test_unaryProductions(self):
        g = self.__simpleGrammar()
        self.assertEqual(len(g.unaryProductions('A')), 1)

    def test_initializeCkyTable(self):
        g = self.__simpleGrammar()        
        cky = CKY(g, ['a', 'b'])
        self.assertEqual(len(cky.table[0][0]), 2) # A, F
        pass

if __name__ == "__main__":
    unittest.main()



