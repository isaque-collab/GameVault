import GameCard from '../GameCard/GameCard.tsx';
import './GameSection.css'

export type GameSectionItem = {
    id: number
    nome: string
    imagemUrl?: string
    avaliacao?: number
    anoLancamento?: number
}

type GameSectionProps = {
    titulo: string
    jogos: GameSectionItem[]
}

function GameSection({titulo, jogos }: GameSectionProps) {
    return (
        <section className="game-section">
            <h2 className="game-section_title">{titulo}</h2>

            <div className="game-section_games">
                {jogos.map((jogo) => (
                    <GameCard
                        key={jogo.id}
                        id={jogo.id}
                        nome={jogo.nome}
                        imagemUrl={jogo.imagemUrl}
                        avaliacao={jogo.avaliacao}
                        anoLancamento={jogo.anoLancamento}
                    />
                ))}
            </div>
        </section>
    )
}

export default GameSection;