
class GrammarSymbol:
    """Names for productions"""
    def __init__(self, name):
        self.name = name

    def isTerminal(self):
        return False

    def isNonterminal(self):
        return False

class N(GrammarSymbol):
    def isNonterminal(self):
        return True

class T(GrammarSymbol):
    def isTerminal(self):
        return True


def productionHierarchyString(aProduction, indent=0):
    res = (" " * indent) + str(aProduction) + "\n"
    for elem in aProduction.tree:
        res += (" " * indent) + productionHierarchyString(elem, indent + 4)
    return res 

def productionHierarchyTreebankFormat(aProduction, indent=0):
    res = (" " * indent) + "(" + aProduction.name
    if aProduction.p1.isTerminal() and aProduction.p2 == None:
        res += " " + aProduction.p1.name
    if len(aProduction.tree) > 0:
        for prod in aProduction.tree:
            res += "\n" + (" " * indent) + \
                productionHierarchyTreebankFormat(prod, indent + 4)
    res += ")"
    return res


class Production:
    """Production in Chomsky NF"""

    def __init__(self, name, p1, p2, probability, tree=[]):
        self.name = name
        self.p1 = p1
        self.p2 = p2
        self.probability = probability
        self.tree = tree

    def __str__(self):
        res = self.name + " -> " + self.p1.name
        if self.p2:
            res += " " + self.p2.name
        return res + " (" + str(self.probability) + ")"

    def __eq__(self, prod):
        if isinstance(prod, self.__class__):
            return self.name == prod.name and \
                self.p1.name == prod.p1.name and \
                ((self.p2.name == prod.p2.name) if prod.p2 else True)
        return False

    def constructFrom(self, leftProd, rightProd):
        tree = []
        if leftProd:
            tree.append(leftProd)
        if rightProd:
            tree.append(rightProd)
        return Production( \
            self.name, \
                self.p1, \
                self.p2, \
                self.probability * leftProd.probability * \
                    (rightProd.probability if rightProd else 1), \
                tree)


class Grammar:
    """CF Grammar in Chomsky NF. Productions must be in CNF."""

    def __init__(self, prods):
        self.prods = prods

    def productions(self, name):
        return filter(lambda x: x.name == name, self.prods)

    def terminalProductions(self, terminal):
        return filter(lambda x: x.p1.isTerminal() and \
                          x.p1.name == terminal, \
                          self.prods)

    def p1Occurrences(self, name):
        return filter(lambda x: x.p1.name == name, \
                          self.prods)
        
    def productionsContaining(self, p1name, p2name):
        return filter(lambda x: x.p2 and x.p2.name == p2name, \
                          self.p1Occurrences(p1name))

    def unaryProductions(self, rHandSide):
        return filter(lambda x: x.p2 == None and x.p1.name == rHandSide, \
                          self.prods)

    def __str__(self):
        return " --- a Grammar: ---\n" + \
            reduce(lambda s, e: s + str(e) + "\n", self.prods, '')
    

class CKY:
    """CKY algorithm"""

    def __init__(self, grammar, sentence):
        print("CKY: table size is " + str(len(sentence)))
        self.grammar = grammar
        self.sentence = sentence
        if (len(sentence) < 1):
            raise Error("Sentence must contain words")
        self.createTable()

    def createTable(self):
        self.tableSize = len(self.sentence)
        self.table = []
        for i in range(self.tableSize):
            col = []
            for j in range(self.tableSize):
                col.append([])
            self.table.append(col)
        self.initializeDiagonal()

    def printTable(self):
        res = ""
        rowIndex = 0
        for row in self.table:
            res += str(rowIndex)
            for cell in row:
                res += "\t| "
                for prod in cell:
                    res += str(prod) + "; "
            res += "\t|\n"
            rowIndex += 1
        print(res)
        return res

    def initializeDiagonal(self):
        tableSize = self.tableSize
        for i in range(tableSize):
            self.table[i][i].extend( \
                self.grammar.terminalProductions(self.sentence[i]))
            self.addUnariesToCell(i, i)
        self.printTable()
        

    def parse(self):
        tableSize = self.tableSize

        # CKY below primary diagonal        
        for k in range(tableSize - 1):
            for pos in self.nthDiagonalIndices(k):
                i, j = pos
                for l in range(j, i):
                    self.table[i][j].extend( \
                        self.findProductionsBetween(l, j, i, l + 1))
                    self.addUnariesToCell(i, j)

        self.printTable()
        return self.table[tableSize - 1][0]
    
    def findProductionsBetween(self, i1, j1, i2, j2):
        result = []
        for prodLeft in self.table[i1][j1]:
            for prodRight in self.table[i2][j2]:
                newProductions = self.grammar.productionsContaining( \
                        prodLeft.name, prodRight.name)
                # bad idea: need to use productions with history
                for newProd in newProductions:
                    result.append(newProd.constructFrom(prodLeft, prodRight))
        return result
    
    def nthDiagonalIndices(self, n):
        result = []
        tableSize = len(self.table)
        for k in range(n, tableSize - 1):
            result.append([k + 1, k - n])
        print(result)
        return result

    def addUnariesToCell(self, i, j):
        cell = self.table[i][j]
        fixed = self.__addUnariesStep(i, j)
        while fixed:
            fixed = self.__addUnariesStep(i, j)
    
    def __addUnariesStep(self, i, j):
        fixed = False
        cell = self.table[i][j]
        for p in cell:
            unariesToP = self.grammar.unaryProductions(p.name)
            for up in unariesToP:
                newProd = up.constructFrom(p, None)
                if newProd in cell:
                    continue
                cell.append(newProd)
                fixed = True
        return fixed

# ---- I/O

def withLinesDo(fileName, block):
    with open(fileName) as f:
        return block(f.xreadlines())
    return []

class GrammarCreator:
    def __init__(self, terminalsFile, nonterminalsFile):
        self.tFile = terminalsFile
        self.nFile = nonterminalsFile

    def createGrammar(self):
        productions = []
        productions.extend(self.createProductions(self.tFile, T))
        productions.extend(self.createProductions(self.nFile, N))
        return Grammar(productions)

    def createProductions(self, fileName, constructor):
        return map(lambda line: Production( \
                line[1], 
                constructor(line[2]), 
                (constructor(line[3]) if len(line) > 3 else None), 
                float(line[0])), \
                       self.splitLines(fileName))
        
    def splitLines(self, fileName):
        lines = withLinesDo(fileName, \
                                lambda lines: \
                                map(lambda line: line.strip().split('\t'), \
                                        lines))
        return filter(lambda x: len(x) > 0, lines)


class SentencesParser:
    def __init__(self, terminalsFile, nonterminalsFile, inFile, outFile):
        self.tFile = terminalsFile
        self.nFile = nonterminalsFile
        self.inFile = inFile
        self.outFile = outFile
        self.grammar = GrammarCreator(terminalsFile, nonterminalsFile). \
            createGrammar()

    def tokenizeSentences(self):
        return withLinesDo(self.inFile, lambda lines: \
                               map(lambda line: line.strip().split(), lines))

    def parseSentence(self, wordsArray):
        cky = CKY(self.grammar, wordsArray)
        return cky.parse()

    def writeParseTrees(self, wordsArray, outFile):
        i = 0
        for prod in self.parseSentence(wordsArray):
            print(productionHierarchyString(prod, 4))
            outFile.write(productionHierarchyTreebankFormat(prod) + "\n")
            i += 1

    def writeAllParseTrees(self):
        with open(self.outFile, "w") as outFile:
            for s in self.tokenizeSentences():
                self.writeParseTrees(s, outFile)


if __name__ == "__main__":
    sp = SentencesParser('T-Rules.txt', 'NT-Rules.txt', \
                                 'Test.txt', 'output.txt')
    sp.writeAllParseTrees()

