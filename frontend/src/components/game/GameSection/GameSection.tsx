import {
    useRef,
} from 'react'
import GameCard from '../GameCard/GameCard.tsx'
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

function GameSection({
                         titulo,
                         jogos,
                     }: GameSectionProps) {
    const listaRef =
        useRef<HTMLDivElement>(null)

    function navegar(direcao: 'anterior' | 'proximo') {
        const lista = listaRef.current

        if (!lista) {
            return
        }

        const distancia =
            lista.clientWidth * 0.8

        lista.scrollBy({
            left:
                direcao === 'proximo'
                    ? distancia
                    : -distancia,
            behavior: 'smooth',
        })
    }

    return (
        <section className="game-section">
            <div className="game-section_header">
                <h2 className="game-section_title">
                    {titulo}
                </h2>

                <div
                    className="game-section_controls"
                    aria-label={`Navegação da seção ${titulo}`}
                >
                    <button
                        className="game-section_button"
                        type="button"
                        aria-label={`Ver jogos anteriores em ${titulo}`}
                        onClick={() =>
                            navegar('anterior')
                        }
                    >
                        <svg
                            viewBox="0 0 24 24"
                            aria-hidden="true"
                        >
                            <path
                                d="m15 18-6-6 6-6"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                            />
                        </svg>
                    </button>

                    <button
                        className="game-section_button game-section_button--primary"
                        type="button"
                        aria-label={`Ver próximos jogos em ${titulo}`}
                        onClick={() =>
                            navegar('proximo')
                        }
                    >
                        <svg
                            viewBox="0 0 24 24"
                            aria-hidden="true"
                        >
                            <path
                                d="m9 18 6-6-6-6"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                            />
                        </svg>
                    </button>
                </div>
            </div>

            <div
                ref={listaRef}
                className="game-section_list"
            >
                {jogos.map((jogo) => (
                    <div
                        className="game-section_item"
                        key={jogo.id}
                    >
                        <GameCard
                            id={jogo.id}
                            nome={jogo.nome}
                            imagemUrl={jogo.imagemUrl}
                            avaliacao={jogo.avaliacao}
                            anoLancamento={
                                jogo.anoLancamento
                            }
                        />
                    </div>
                ))}
            </div>
        </section>
    )
}

export default GameSection