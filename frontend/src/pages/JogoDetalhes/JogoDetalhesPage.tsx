import {
    useCallback,
    useEffect,
    useState,
} from 'react'
import {
    Link,
    useParams
} from 'react-router'
import {
    avaliarJogo,
    removerAvaliacao,
} from '../../api/avaliacoesApi'
import {
    adicionarFavorito,
    adicionarListaDesejos,
    removerFavorito,
    removerListaDesejos,
} from '../../api/colecoesApi'
import {buscarDetalhesJogo} from '../../api/jogosApi'
import {useAuth} from '../../auth/useAuth'
import type {JogoDetalhes} from '../../types/jogo'
import './JogoDetalhesPage.css'

function JogoDetalhesPage() {
    const {rawgGameId} = useParams()
    const {usuario} = useAuth()
    const usuarioAutenticado = usuario !== null

    const [jogo, setJogo] =
        useState<JogoDetalhes | null>(null)

    const [carregando, setCarregando] =
        useState(true)

    const [erro, setErro] =
        useState<string | null>(null)

    const [erroAcao, setErroAcao] =
        useState<string | null>(null)

    const [
        processandoFavorito,
        setProcessandoFavorito,
    ] = useState(false)

    const [
        processandoListaDesejos,
        setProcessandoListaDesejos,
    ] = useState(false)

    const [
        processandoAvaliacao,
        setProcessandoAvaliacao,
    ] = useState(false)

    const [nota, setNota] = useState('')
    const [abaAtiva, setAbaAtiva] = useState('sobre')

    const carregarDetalhes = useCallback(
        async (id: number) => {
            const resposta = await buscarDetalhesJogo(id)

            setJogo(resposta)

            setNota(
                resposta.minhaAvaliacao !== null
                    ? String(resposta.minhaAvaliacao)
                    : '',
            )
        },
        [],
    )

    useEffect(() => {
        async function carregarJogo() {
            const id = Number(rawgGameId)

            if (
                !rawgGameId ||
                Number.isNaN(id) ||
                id < 1
            ) {
                setErro(
                    'Identificador de jogo inválido.',
                )
                setCarregando(false)
                return
            }

            try {
                setCarregando(true)
                setErro(null)
                setJogo(null)

                await carregarDetalhes(id)
            } catch (error) {
                console.error(error)

                setErro(
                    'Não foi possível carregar os detalhes do jogo.',
                )
            } finally {
                setCarregando(false)
            }
        }

        carregarJogo()
    }, [rawgGameId, carregarDetalhes])

    async function alternarFavorito() {
        if (
            !jogo ||
            jogo.favoritado === null
        ) {
            return
        }

        const novoEstado = !jogo.favoritado

        try {
            setProcessandoFavorito(true)
            setErroAcao(null)

            if (jogo.favoritado) {
                await removerFavorito(
                    jogo.rawgGameId,
                )
            } else {
                await adicionarFavorito(
                    jogo.rawgGameId,
                )
            }

            setJogo((jogoAtual) =>
                jogoAtual
                    ? {
                        ...jogoAtual,
                        favoritado: novoEstado,
                    }
                    : jogoAtual,
            )
        } catch (error) {
            console.error(error)

            setErroAcao(
                'Não foi possível atualizar os favoritos.',
            )
        } finally {
            setProcessandoFavorito(false)
        }
    }

    async function alternarListaDesejos() {
        if (
            !jogo ||
            jogo.naListaDesejos === null
        ) {
            return
        }

        const novoEstado =
            !jogo.naListaDesejos

        try {
            setProcessandoListaDesejos(true)
            setErroAcao(null)

            if (jogo.naListaDesejos) {
                await removerListaDesejos(
                    jogo.rawgGameId,
                )
            } else {
                await adicionarListaDesejos(
                    jogo.rawgGameId,
                )
            }

            setJogo((jogoAtual) =>
                jogoAtual
                    ? {
                        ...jogoAtual,
                        naListaDesejos:
                        novoEstado,
                    }
                    : jogoAtual,
            )
        } catch (error) {
            console.error(error)

            setErroAcao(
                'Não foi possível atualizar a lista de desejos.',
            )
        } finally {
            setProcessandoListaDesejos(false)
        }
    }

    async function enviarAvaliacao(notaSelecionada = Number(nota)) {
        if (!jogo) {
            return
        }

        const notaNumerica = notaSelecionada

        if (
            Number.isNaN(notaNumerica) ||
            notaNumerica < 1 ||
            notaNumerica > 5
        ) {
            setErroAcao(
                'Selecione uma nota entre 1 e 5 estrelas.',
            )
            return
        }

        try {
            setProcessandoAvaliacao(true)
            setErroAcao(null)

            await avaliarJogo(
                jogo.rawgGameId,
                notaNumerica,
            )

            await carregarDetalhes(
                jogo.rawgGameId,
            )
        } catch (error) {
            console.error(error)

            setErroAcao(
                'Não foi possível salvar sua avaliação.',
            )
        } finally {
            setProcessandoAvaliacao(false)
        }
    }

    async function excluirAvaliacao() {
        if (
            !jogo ||
            jogo.minhaAvaliacao === null
        ) {
            return
        }

        try {
            setProcessandoAvaliacao(true)
            setErroAcao(null)

            await removerAvaliacao(
                jogo.rawgGameId,
            )

            await carregarDetalhes(
                jogo.rawgGameId,
            )
        } catch (error) {
            console.error(error)

            setErroAcao(
                'Não foi possível remover sua avaliação.',
            )
        } finally {
            setProcessandoAvaliacao(false)
        }
    }

    if (carregando) {
        return (
            <main className="jogo-detalhes">
                <p>Carregando detalhes do jogo...</p>
            </main>
        )
    }

    if (erro || !jogo) {
        return (
            <main className="jogo-detalhes">
                <p className="jogo-detalhes__erro" role="alert">
                    {erro ?? 'Jogo não encontrado.'}
                </p>
            </main>
        )
    }

    return (
        <main className="jogo-detalhes">
            <section
                className="jogo-detalhes__hero"
                style={{
                    backgroundImage: jogo.imagemFundo
                        ? 'linear-gradient(90deg, rgb(11 16 23 / 98%) 0%, rgb(11 16 23 / 88%) 48%, rgb(11 16 23 / 55%) 100%), url("' + jogo.imagemFundo + '")'
                        : undefined,
                }}
            >
                {jogo.imagemFundo && (
                    <div className="jogo-detalhes__capa">
                        <img
                            src={jogo.imagemFundo}
                            alt={'Capa de ' + jogo.nome}
                        />
                    </div>
                )}

                <div className="jogo-detalhes__conteudo">
                    <span className="jogo-detalhes__label">DETALHES DO JOGO</span>
                    <h1>{jogo.nome}</h1>

                    <div className="jogo-detalhes__meta">
                        {jogo.dataLancamento && <span>{jogo.dataLancamento.slice(0, 4)}</span>}
                        {jogo.generos.slice(0, 3).map((genero) => (
                            <span key={genero}>{genero}</span>
                        ))}
                        {jogo.desenvolvedoras[0] && <span>{jogo.desenvolvedoras[0]}</span>}
                    </div>

                    <div className="jogo-detalhes__avaliacoes">
                        {jogo.notaRawg !== null && (
                            <div className="jogo-detalhes__rating">
                                <strong>{jogo.notaRawg}</strong>
                                <span>RAWG</span>
                            </div>
                        )}
                        {jogo.metacritic !== null && (
                            <div className="jogo-detalhes__metacritic">
                                <strong>{jogo.metacritic}</strong>
                                <span>METACRITIC</span>
                            </div>
                        )}
                        {jogo.mediaAvaliacoesGameVault !== null && (
                            <div className="jogo-detalhes__gv-rating">
                                <strong>{jogo.mediaAvaliacoesGameVault}</strong>
                                <span>
                                    GAMEVAULT
                                    {jogo.quantidadeAvaliacoesGameVault > 0
                                        ? ' · ' + jogo.quantidadeAvaliacoesGameVault + ' avaliações'
                                        : ''}
                                </span>
                            </div>
                        )}
                    </div>

                    <div className="jogo-detalhes__acoes">
                        {usuarioAutenticado ? (
                            <>
                                <button
                                    type="button"
                                    className="jogo-detalhes__acao jogo-detalhes__acao--primary"
                                    onClick={alternarFavorito}
                                    disabled={processandoFavorito}
                                >
                                    <span aria-hidden="true">♥</span>
                                    {processandoFavorito
                                        ? 'Atualizando...'
                                        : jogo.favoritado ? 'Remover favorito' : 'Favoritar'}
                                </button>
                                <button
                                    type="button"
                                    className="jogo-detalhes__acao"
                                    onClick={alternarListaDesejos}
                                    disabled={processandoListaDesejos}
                                >
                                    <span aria-hidden="true">☷</span>
                                    {processandoListaDesejos
                                        ? 'Atualizando...'
                                        : jogo.naListaDesejos ? 'Remover da lista' : 'Lista de desejos'}
                                </button>
                            </>
                        ) : (
                            <Link
                                className="jogo-detalhes__acao jogo-detalhes__acao--primary"
                                to="/login"
                            >
                                Entrar para interagir
                            </Link>
                        )}
            {usuarioAutenticado && (
                            <div className="jogo-detalhes__minha-avaliacao">
                                <span className="jogo-detalhes__minha-avaliacao-label">
                                    Minha avaliação
                                </span>
                                <div className="jogo-detalhes__estrelas" aria-label="Avalie este jogo de 1 a 5 estrelas">
                                    {[1, 2, 3, 4, 5].map((valor) => (
                                        <button
                                            key={valor}
                                            type="button"
                                            className={
                                                'jogo-detalhes__estrela' +
                                                (jogo.minhaAvaliacao !== null && valor <= jogo.minhaAvaliacao
                                                    ? ' jogo-detalhes__estrela--selecionada'
                                                    : '')
                                            }
                                            data-nota={valor}
                                            aria-label={valor + (valor === 1 ? ' estrela' : ' estrelas')}
                                            aria-pressed={jogo.minhaAvaliacao === valor}
                                            title={valor + (valor === 1 ? ' estrela' : ' estrelas')}
                                            onClick={() => {
                                                setNota(String(valor))
                                                enviarAvaliacao(valor)
                                            }}
                                            disabled={processandoAvaliacao}
                                        >
                                            ★
                                        </button>
                                    ))}
                                </div>
                                {jogo.minhaAvaliacao !== null && (
                                    <button
                                        type="button"
                                        className="jogo-detalhes__remover-avaliacao"
                                        onClick={excluirAvaliacao}
                                        disabled={processandoAvaliacao}
                                    >
                                        Remover
                                    </button>
                                )}
                            </div>
                        )}

                    </div>

                    {(jogo.tempoMedioJogo !== null || jogo.classificacaoEtaria) && (
                        <div className="jogo-detalhes__ficha">
                            {jogo.tempoMedioJogo !== null && (
                                <div>
                                    <span>Tempo médio</span>
                                    <strong>{jogo.tempoMedioJogo}h</strong>
                                </div>
                            )}
                            {jogo.classificacaoEtaria && (
                                <div>
                                    <span>Classificação</span>
                                    <strong>{jogo.classificacaoEtaria}</strong>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </section>

            <nav className="jogo-detalhes__abas" aria-label="Seções dos detalhes do jogo">
                <button
                    type="button"
                    className={abaAtiva === 'sobre' ? 'ativo' : ''}
                    onClick={() => setAbaAtiva('sobre')}
                >
                    Sobre
                </button>

                {jogo.screenshots.length > 0 && (
                    <button
                        type="button"
                        className={abaAtiva === 'screenshots' ? 'ativo' : ''}
                        onClick={() => setAbaAtiva('screenshots')}
                    >
                        Screenshots
                    </button>
                )}

                {jogo.plataformas.length > 0 && (
                    <button
                        type="button"
                        className={abaAtiva === 'plataformas' ? 'ativo' : ''}
                        onClick={() => setAbaAtiva('plataformas')}
                    >
                        Plataformas
                    </button>
                )}

                {jogo.plataformas.some(
                    (plataforma) =>
                        plataforma.requisitoMinimo ||
                        plataforma.requisitoRecomendade,
                ) && (
                    <button
                        type="button"
                        className={abaAtiva === 'requisitos' ? 'ativo' : ''}
                        onClick={() => setAbaAtiva('requisitos')}
                    >
                        Requisitos
                    </button>
                )}
            </nav>

            {abaAtiva === 'sobre' && (
                <section className="jogo-detalhes__bloco">
                    <div className="jogo-detalhes__bloco-cabecalho">
                        <span>Sobre</span>
                    </div>

                    {jogo.descricao ? (
                        <p className="jogo-detalhes__descricao">{jogo.descricao}</p>
                    ) : (
                        <p className="jogo-detalhes__vazio">Descrição não disponível.</p>
                    )}

                    {jogo.generos.length > 0 && (
                        <div className="jogo-detalhes__tags">
                            {jogo.generos.map((genero) => <span key={genero}>{genero}</span>)}
                        </div>
                    )}

                    <div className="jogo-detalhes__creditos">
                        {jogo.desenvolvedoras.length > 0 && (
                            <div>
                                <span>Desenvolvedora</span>
                                <strong>{jogo.desenvolvedoras.join(', ')}</strong>
                            </div>
                        )}
                        {jogo.publicadoras.length > 0 && (
                            <div>
                                <span>Publicadora</span>
                                <strong>{jogo.publicadoras.join(', ')}</strong>
                            </div>
                        )}
                    </div>
                </section>
            )}

            {abaAtiva === 'screenshots' && jogo.screenshots.length > 0 && (
                <section className="jogo-detalhes__bloco">
                    <div className="jogo-detalhes__bloco-cabecalho">
                        <span>Screenshots</span>
                        <small>{jogo.screenshots.length} imagens</small>
                    </div>
                    <div className="jogo-detalhes__screenshots">
                        {jogo.screenshots.map((screenshot) => (
                            <img
                                key={screenshot}
                                src={screenshot}
                                alt={'Screenshot de ' + jogo.nome}
                            />
                        ))}
                    </div>
                </section>
            )}

            {abaAtiva === 'plataformas' && jogo.plataformas.length > 0 && (
                <section className="jogo-detalhes__bloco">
                    <div className="jogo-detalhes__bloco-cabecalho">
                        <span>Plataformas</span>
                    </div>
                    <div className="jogo-detalhes__plataformas">
                        {jogo.plataformas.map((plataforma) => (
                            <div key={plataforma.id} className="jogo-detalhes__plataforma">
                                <strong>{plataforma.nome}</strong>
                            </div>
                        ))}
                    </div>
                </section>
            )}

            {abaAtiva === 'requisitos' && (
                <section className="jogo-detalhes__bloco">
                    <div className="jogo-detalhes__bloco-cabecalho">
                        <span>Requisitos</span>
                    </div>
                    <div className="jogo-detalhes__requisitos">
                        {jogo.plataformas.map((plataforma) =>
                            (plataforma.requisitoMinimo || plataforma.requisitoRecomendade) && (
                                <div key={plataforma.id}>
                                    <strong>{plataforma.nome}</strong>
                                    {plataforma.requisitoMinimo && (
                                        <p><span>Mínimo</span>{plataforma.requisitoMinimo}</p>
                                    )}
                                    {plataforma.requisitoRecomendade && (
                                        <p><span>Recomendado</span>{plataforma.requisitoRecomendade}</p>
                                    )}
                                </div>
                            ),
                        )}
                    </div>
                </section>
            )}


            {erroAcao && (
                <p className="jogo-detalhes__erro" role="alert">{erroAcao}</p>
            )}
        </main>
    )

}

export default JogoDetalhesPage