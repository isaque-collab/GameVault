import './GameCard.css'

type GameCardProps = {
    nome: string
    imagemUrl?: string
    avaliacao?: number
    anoLancamento?: number
}

function GameCard({
                      nome,
                      imagemUrl,
                      avaliacao,
                      anoLancamento,
                  }: GameCardProps) {
    return (
        <article className="game-card">
            {imagemUrl ? (
                <img
                    className="game-card_image"
                    src={imagemUrl}
                    alt={`Capa do jogo ${nome}`}
                    />
            ) : (
                <div className="game-card_image-placeholder">
                    Sem imagem
                </div>
            )}

            <div className="game-card_content">
                <h3 className="game-card_title">{nome}</h3>

                {avaliacao !== undefined && (
                    <p className="game-card_info">
                        Avaliacao: {avaliacao}
                    </p>
                )}

                {anoLancamento !== undefined && (
                    <p className="game-card_info">
                        Lançamento: {anoLancamento}
                    </p>
                )}
            </div>
        </article>
    )
}

export default GameCard