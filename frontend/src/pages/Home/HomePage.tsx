import {
    useEffect,
    useState,
} from 'react'
import {
    Link,
} from 'react-router'
import {
    buscarJogosMaisBemAvaliados,
    buscarJogosPopulares,
    buscarLancamentosRecentes,
} from '../../api/jogosApi.ts'
import GameSection, {
    type GameSectionItem,
} from '../../components/game/GameSection/GameSection.tsx'
import type {JogoResumo} from '../../types/jogo.ts'
import './HomePage.css'

function converterParaGameSectionItem(
    jogo: JogoResumo,
): GameSectionItem {
    return {
        id: jogo.rawgGameId,
        nome: jogo.nome,
        imagemUrl: jogo.imagemFundo ?? undefined,
        avaliacao: jogo.notaRawg ?? undefined,
        anoLancamento: jogo.dataLancamento
            ? Number(jogo.dataLancamento.slice(0, 4))
            : undefined,
    }
}

function HomePage() {
    const [populares, setPopulares] =
        useState<GameSectionItem[]>([])

    const [lancamentos, setLancamentos] =
        useState<GameSectionItem[]>([])

    const [maisBemAvaliados, setMaisBemAvaliados] =
        useState<GameSectionItem[]>([])

    const [carregando, setCarregando] =
        useState(true)

    const [erro, setErro] =
        useState<string | null>(null)

    useEffect(() => {
        async function carregarJogos() {
            try {
                setCarregando(true)
                setErro(null)

                const [
                    respostaPopulares,
                    respostaLancamentos,
                    respostaMaisBemAvaliados,
                ] = await Promise.all([
                    buscarJogosPopulares(),
                    buscarLancamentosRecentes(),
                    buscarJogosMaisBemAvaliados(),
                ])

                setPopulares(
                    respostaPopulares.jogos.map(
                        converterParaGameSectionItem,
                    ),
                )

                setLancamentos(
                    respostaLancamentos.jogos.map(
                        converterParaGameSectionItem,
                    ),
                )

                setMaisBemAvaliados(
                    respostaMaisBemAvaliados.jogos.map(
                        converterParaGameSectionItem,
                    ),
                )
            } catch (error) {
                console.error(error)

                setErro(
                    'Não foi possível carregar os jogos.',
                )
            } finally {
                setCarregando(false)
            }
        }

        carregarJogos()
    }, [])

    if (carregando) {
        return (
            <main className="home">
                <div className="home_status">
                    <p>Carregando jogos...</p>
                </div>
            </main>
        )
    }

    if (erro) {
        return (
            <main className="home">
                <div className="home_status">
                    <p role="alert">{erro}</p>
                </div>
            </main>
        )
    }

    const destaque = populares[0]

    const popularesDaSecao =
        populares.slice(1)

    return (
        <main className="home">
            {destaque && (
                <section
                    className="home_hero"
                    style={
                        destaque.imagemUrl
                            ? {
                                backgroundImage:
                                    `url("${destaque.imagemUrl}")`,
                            }
                            : undefined
                    }
                >
                    {destaque.imagemUrl && (
                        <img
                            className="home_hero-image"
                            src={destaque.imagemUrl}
                            alt=""
                            aria-hidden="true"
                        />
                    )}
                    <div className="home_hero-overlay"/>

                    <div className="home_hero-content">
                        <span className="home_hero-label">
                            Em destaque
                        </span>

                        <h1 className="home_hero-title">
                            {destaque.nome}
                        </h1>

                        <p className="home_hero-description">
                            Descubra detalhes, avaliações e
                            informações sobre um dos jogos em
                            destaque no GameVault.
                        </p>

                        <div className="home_hero-metadata">
                            {destaque.anoLancamento !==
                                undefined && (
                                    <span>
                                    {destaque.anoLancamento}
                                </span>
                                )}

                            {destaque.avaliacao !==
                                undefined && (
                                    <span>
                                    RAWG{' '}
                                        {destaque.avaliacao.toLocaleString(
                                            'pt-BR',
                                            {
                                                minimumFractionDigits: 1,
                                                maximumFractionDigits: 1,
                                            },
                                        )}
                                </span>
                                )}
                        </div>

                        <div className="home_hero-actions">
                            <Link
                                className="home_hero-primary"
                                to={`/jogos/${destaque.id}`}
                            >
                                Ver detalhes
                            </Link>

                            <Link
                                className="home_hero-secondary"
                                to="/catalogo"
                            >
                                Explorar catálogo
                            </Link>
                        </div>
                    </div>
                </section>
            )}

            <div className="home_sections">
                <GameSection
                    titulo="Populares"
                    jogos={popularesDaSecao}
                />

                <GameSection
                    titulo="Lançamentos recentes"
                    jogos={lancamentos}
                />

                <GameSection
                    titulo="Mais bem avaliados"
                    jogos={maisBemAvaliados}
                />
            </div>
        </main>
    )
}

export default HomePage