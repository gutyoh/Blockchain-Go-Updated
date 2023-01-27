package main

import (
	"crypto/sha256"
	"fmt"
	"math/rand"
	"strings"
	"time"
)

const (
	nIncreased = "N was increased to %d"
	nDecreased = "N was decreased by 1"
	nStays     = "N stays the same"
)

type Block struct {
	ID           uint
	Timestamp    time.Time
	MagicNumber  int32
	PreviousHash string
	Hash         string
	BuildTime    int64
	Miner        uint
}

func (b *Block) CalculateHash() string {
	magicNumber := fmt.Sprintf("%d", b.MagicNumber)
	id := fmt.Sprintf("%d", b.ID)
	timestamp := fmt.Sprintf("%s", b.Timestamp)

	sum := sha256.Sum256([]byte(id + timestamp + b.PreviousHash + magicNumber))
	return fmt.Sprintf("%x", sum)
}

type Blockchain struct {
	Chain []*Block
}

func (bc *Blockchain) Init() {
	bc.Chain = []*Block{bc.CreateGenesisBlock()}
}

func (bc *Blockchain) CreateGenesisBlock() *Block {
	timestamp := time.Now()
	magicNumber := rand.Int31()
	miner := rand.Intn(10)
	hash := sha256.Sum256([]byte("Genesis block" + fmt.Sprintf("%d", magicNumber)))

	return &Block{ID: 1, Hash: fmt.Sprintf("%x", hash), MagicNumber: magicNumber, Miner: uint(miner),
		Timestamp: timestamp, PreviousHash: "0" /*Data: []string{"no messages"}*/}
}

func (bc *Blockchain) Print(nState string) {
	// Get the last block of the blockchain
	lastBlock := bc.Chain[len(bc.Chain)-1]

	if lastBlock.ID == 1 {
		fmt.Printf("Genesis Block:\n")
	}

	if lastBlock.ID > 1 {
		fmt.Printf("\nBlock:\n")
	}

	fmt.Printf("Created by miner #%d\n", lastBlock.Miner)
	fmt.Printf("Id: %d\n", lastBlock.ID)
	fmt.Printf("Timestamp: %d\n", lastBlock.Timestamp.UnixMilli())
	fmt.Printf("Magic number: %d\n", lastBlock.MagicNumber)
	fmt.Printf("Hash of the previous block:\n%s\n", lastBlock.PreviousHash)
	fmt.Printf("Hash of the block:\n%s\n", lastBlock.Hash)
	fmt.Printf("Block was generating for %d seconds\n", lastBlock.BuildTime)
	fmt.Printf("%s\n", nState)
}

// ======================== HELPER FUNCTIONS ========================

func PrintGenesisBlock(difficulty int, hyperCoin *Blockchain, prefix string) (int, string) {
	difficulty++
	hyperCoin.Print(fmt.Sprintf(nIncreased, difficulty))
	prefix = strings.Repeat("0", difficulty)
	return difficulty, prefix
}

func FindBlock(prefix string, b *Block, done chan struct{}) {
	for {
		select {
		case <-done:
			return
		default:
			b.MagicNumber = rand.Int31()
			b.Hash = b.CalculateHash()
			if strings.HasPrefix(b.Hash, prefix) {
				return
			}
		}
	}
}

func MineBlock(prevBlock *Block, prefix string, creator uint, next chan Block, done chan struct{}) {
	start := time.Now()
	b := Block{
		ID:           prevBlock.ID + 1,
		PreviousHash: prevBlock.Hash,
	}

	FindBlock(prefix, &b, done)

	b.Timestamp = time.Now()
	b.BuildTime = int64(time.Since(start).Seconds())
	b.Miner = creator
	next <- b
}

func MineNewBlockAndUpdateDifficulty(hyperCoin *Blockchain, prefix string, difficulty int) {
	for i := 0; i < 4; i++ {
		next := make(chan Block)
		done := make(chan struct{})

		rand.Seed(time.Now().UnixNano())
		creator := rand.Intn(10) + 1

		go MineBlock(hyperCoin.Chain[i], prefix, uint(creator), next, done)

		newBlock := <-next

		close(done)

		hyperCoin.Chain = append(hyperCoin.Chain, &newBlock)
		var nState string

		switch {
		case newBlock.BuildTime < 5:
			difficulty++
			nState = fmt.Sprintf(nIncreased, difficulty)
			prefix = strings.Repeat("0", difficulty)
		case newBlock.BuildTime > 10:
			difficulty--
			nState = nDecreased
			prefix = strings.Repeat("0", difficulty)
		default:
			nState = nStays
		}
		hyperCoin.Print(nState)
	}
}

func main() {
	var difficulty int
	var prefix string

	hyperCoin := new(Blockchain)
	hyperCoin.Init()

	difficulty, prefix = PrintGenesisBlock(difficulty, hyperCoin, prefix)

	MineNewBlockAndUpdateDifficulty(hyperCoin, prefix, difficulty)
}
