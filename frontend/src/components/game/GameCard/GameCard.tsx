import './GameCard.css'
import {Link} from 'react-router'

type GameCardProps = {
    id: number
    nome: string
    imagemUrl?: string
    avaliacao?: number
    anoLancamento?: number
}

function GameCard({
                      id,
                      nome,
                      imagemUrl,
                      avaliacao,
                      anoLancamento,
                  }: GameCardProps) {
    const avaliacaoFormatada =
        avaliacao?.toLocaleString('pt-BR', {
            minimumFractionDigits: 1,
            maximumFractionDigits: 1,
        })

    return (
        <Link
            className="game-card"
            to={`/jogos/${id}`}
        >
            <div className="game-card_media">
                {imagemUrl ? (
                    <img
                        className="game-card_image"
                        src={imagemUrl}
                        alt={`Capa do jogo ${nome}`}
                        loading="lazy"
                    />
                ) : (
                    <div className="game-card_image-placeholder">
                        <span>Sem imagem</span>
                    </div>
                )}
            </div>

            <div className="game-card_content">
                <h3
                    className="game-card_title"
                    title={nome}
                >
                    {nome}
                </h3>

                {(anoLancamento !== undefined ||
                    avaliacao !== undefined) && (
                    <div className="game-card_metadata">
                        {anoLancamento !== undefined && (
                            <span className="game-card_year">
                                {anoLancamento}
                            </span>
                        )}

                        {avaliacao !== undefined && (
                            <span
                                className="game-card_rating"
                                aria-label={`Avaliação RAWG ${avaliacaoFormatada} de 5`}
                            >
                                RAWG {avaliacaoFormatada}
                            </span>
                        )}
                    </div>
                )}
            </div>
        </Link>
    )
}

export default GameCard